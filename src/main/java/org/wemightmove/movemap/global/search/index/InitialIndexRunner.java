package org.wemightmove.movemap.global.search.index;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.global.search.reconcile.ReconciliationJob;

import java.time.Instant;

/**
 * 기동 시 초기 전량 색인 트리거.
 * <p>
 * {@link IndexBootstrapper}(@Order(0), DDL/alias 보장) 이후에 실행되도록 {@code @Order(1)} 로 순서를 고정한다.
 * DDL(부트스트랩)과 데이터(초기 색인)의 책임을 별도 클래스로 분리한다.
 * <p>
 * 도메인별로 alias 문서 수가 0 이면 전량 재색인을 수행하고, 이미 문서가 있으면 스킵한다(멱등 —
 * bulk op=index 는 동일 {@code _id} 를 덮어쓰므로 재실행도 안전하지만, 불필요한 작업을 피하기 위해 비어있을 때만 색인).
 * 처리 후 {@link ReconciliationJob#markInitialized}로 리컨실 워터마크를 현재 시각으로 전진시켜, 첫 스케줄 리컨실이
 * 이미 색인된 데이터를 통째로 재검증하지 않게 한다.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class InitialIndexRunner implements ApplicationRunner {

    private final BulkReindexer bulkReindexer;
    private final ReconciliationJob reconciliationJob;

    @Override
    public void run(ApplicationArguments args) {
        for (SearchDomain domain : SearchDomain.values()) {
            long existing = bulkReindexer.esCount(domain);
            if (existing == 0) {
                log.info("초기 색인: 인덱스 비어있음 → 전량 색인 실행: domain={}, index={}",
                        domain.label(), domain.indexName());
                bulkReindexer.reindex(domain);
            } else {
                log.info("초기 색인: 이미 문서 {}건 존재 → 스킵: domain={}, index={}",
                        existing, domain.label(), domain.indexName());
            }
            reconciliationJob.markInitialized(domain, Instant.now());
        }
    }
}
