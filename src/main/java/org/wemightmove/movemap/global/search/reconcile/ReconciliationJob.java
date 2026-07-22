package org.wemightmove.movemap.global.search.reconcile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.global.config.SearchProperties;
import org.wemightmove.movemap.global.search.SearchMetrics;
import org.wemightmove.movemap.global.search.index.BulkReindexer;
import org.wemightmove.movemap.global.search.index.IndexedDoc;
import org.wemightmove.movemap.global.search.index.SearchDomain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PG↔ES 정합성 리컨실리에이션 스케줄러.
 * <p>
 * 매 실행마다 도메인별로:
 * <ol>
 *   <li><b>델타</b>: {@code updated_at > lastRun} 인 row 를 keyset 로 읽어 content_hash 를 재계산하고,
 *       ES 문서의 hash(mget)와 다르거나 없는 것만 재색인한 뒤, 본 최대 {@code updated_at} 로 워터마크를 전진한다
 *       (델타가 없으면 {@code now()} 로 전진).</li>
 *   <li><b>드리프트 게이지</b>: {@code count(PG) - count(alias)} 를 {@code search_reconciliation_drift{domain}} 로 등록.</li>
 *   <li><b>프레시니스</b>: 성공 시 {@code search_reconciliation_last_success{domain}} 갱신, 드리프트≠0 이면 WARN.</li>
 * </ol>
 * <p>
 * <b>워터마크(lastRun) 결정</b>: 인메모리 {@link AtomicReference}, 기본값 {@link Instant#EPOCH}. 정상 기동에서는
 * {@link #markInitialized}(초기 색인 러너가 인덱스 보장 직후 {@code now()} 로 호출)로 덮어써져 첫 스케줄 실행의 델타가
 * 거의 비게 된다. 초기 색인이 실패해 markInitialized 가 호출되지 않은 경우에만 EPOCH 가 남아 전량을 hash 비교로
 * 자가 검증한다(안전한 폴백). 프로세스 재시작 시 워터마크는 초기화되므로 재기동 후 첫 실행은 다시 markInitialized
 * 시각 기준이다 — 런타임 쓰기 경로가 없는 현재 설계(GLOBAL §5)에서 델타는 보통 비어 있고, <b>드리프트 게이지 +
 * (기동 시)빈 인덱스 재색인</b>이 실질적인 정합성 보증 수단이다.
 * <p>
 * 중첩 실행 방지: {@link Scheduled} 는 기본 단일 스레드라 스택되지 않으나, 향후 스케줄러 풀이 도입돼도 안전하도록
 * {@link AtomicBoolean} 재진입 가드를 둔다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationJob {

    private final SearchProperties searchProperties;
    private final BulkReindexer bulkReindexer;
    private final SearchMetrics searchMetrics;

    private final Map<SearchDomain, AtomicReference<Instant>> watermarks = initialWatermarks();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private static Map<SearchDomain, AtomicReference<Instant>> initialWatermarks() {
        Map<SearchDomain, AtomicReference<Instant>> map = new EnumMap<>(SearchDomain.class);
        for (SearchDomain domain : SearchDomain.values()) {
            map.put(domain, new AtomicReference<>(Instant.EPOCH));
        }
        return map;
    }

    /**
     * 초기 색인 러너가 인덱스를 보장한 직후 호출한다. 델타 워터마크를 주어진 시각으로 전진시켜,
     * 이미 색인된 데이터를 첫 스케줄 실행에서 통째로 재검증하지 않도록 한다.
     */
    public void markInitialized(SearchDomain domain, Instant at) {
        watermarks.get(domain).set(at);
        log.info("리컨실 워터마크 초기화: domain={}, watermark={}", domain.label(), at);
    }

    @Scheduled(cron = "${movemap.search.reconcile.cron:0 */15 * * * *}")
    public void reconcile() {
        if (!searchProperties.reconcile().enabled()) {
            log.debug("리컨실리에이션 비활성화(movemap.search.reconcile.enabled=false), 스킵");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            log.warn("이전 리컨실 실행이 아직 진행 중, 이번 주기 스킵");
            return;
        }
        try {
            for (SearchDomain domain : SearchDomain.values()) {
                reconcileDomain(domain);
            }
        } finally {
            running.set(false);
        }
    }

    private void reconcileDomain(SearchDomain domain) {
        try {
            int reindexed = reconcileDelta(domain);
            long pgCount = bulkReindexer.pgCount(domain);
            long esCount = bulkReindexer.esCount(domain);
            long drift = pgCount - esCount;

            searchMetrics.recordDrift(domain.label(), drift);
            searchMetrics.recordReconciliationSuccess(domain.label(), Instant.now().getEpochSecond());

            if (drift != 0) {
                log.warn("리컨실 드리프트 감지: domain={}, pgCount={}, esCount={}, drift={}, deltaReindexed={}",
                        domain.label(), pgCount, esCount, drift, reindexed);
            } else {
                log.info("리컨실 완료: domain={}, pgCount={}, esCount={}, drift=0, deltaReindexed={}",
                        domain.label(), pgCount, esCount, reindexed);
            }
        } catch (RuntimeException e) {
            // 한 도메인 실패가 다른 도메인/다음 주기를 막지 않도록 격리(다음 주기에 재시도).
            log.error("리컨실 실패: domain={}", domain.label(), e);
        }
    }

    /**
     * 델타를 keyset 로 훑어 hash 가 다르거나 없는 문서만 재색인하고 재색인 건수를 반환한다.
     * 워터마크는 본 최대 updated_at(없으면 now())로 전진한다.
     */
    private int reconcileDelta(SearchDomain domain) {
        Instant since = watermarks.get(domain).get();
        Instant maxSeen = since;
        long afterId = 0;
        int reindexed = 0;

        while (true) {
            List<SearchDomain.MappedRow> page =
                    bulkReindexer.loadDelta(domain, since, afterId, BulkReindexer.PAGE_SIZE);
            if (page.isEmpty()) {
                break;
            }

            List<Long> ids = page.stream().map(row -> row.doc().id()).toList();
            Map<Long, String> existingHashes = bulkReindexer.fetchContentHashes(domain, ids);

            List<IndexedDoc> toReindex = new ArrayList<>();
            for (SearchDomain.MappedRow row : page) {
                IndexedDoc doc = row.doc();
                String existing = existingHashes.get(doc.id());
                if (existing == null || !existing.equals(doc.contentHash())) {
                    toReindex.add(doc);
                }
                if (row.updatedAt() != null && row.updatedAt().isAfter(maxSeen)) {
                    maxSeen = row.updatedAt();
                }
                afterId = doc.id();
            }

            if (!toReindex.isEmpty()) {
                long failed = bulkReindexer.bulkIndex(domain, toReindex);
                reindexed += toReindex.size() - failed;
            }
        }

        if (reindexed > 0) {
            bulkReindexer.refresh(domain);
        }

        Instant newWatermark = maxSeen.isAfter(since) ? maxSeen : Instant.now();
        watermarks.get(domain).set(newWatermark);
        return reindexed;
    }
}
