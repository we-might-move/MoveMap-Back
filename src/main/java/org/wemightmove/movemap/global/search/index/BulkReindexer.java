package org.wemightmove.movemap.global.search.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.get.GetResult;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.global.search.SearchMetrics;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PG(원본, SoT) → ES(파생) 색인 데이터 액세스 허브.
 * <p>
 * 전량 재색인({@link #reindexAll()}, {@link #reindex(SearchDomain)})과, 리컨실리에이션이 재사용하는
 * 저수준 프리미티브(델타 로드, content_hash mget, bulk 색인, count)를 함께 제공한다.
 * <ul>
 *   <li>읽기: {@link EntityManager} native SQL 로 id 오름차순 keyset 스트림(페이지 {@value #PAGE_SIZE}).</li>
 *   <li>쓰기: 저수준 {@link ElasticsearchClient} bulk, op=index, {@code _id}=PK(멱등 upsert).
 *       GLOBAL §6 — bulk 응답의 {@code errors()} 를 검사하고 실패 항목을 ERROR 로그 + 메트릭으로 표면화한다(빈 catch 금지).</li>
 * </ul>
 * 트랜잭션으로 감싸지 않는다: 각 페이지 쿼리가 짧은 커넥션을 쓰고, 그 사이 ES 네트워크 호출이 DB 커넥션을
 * 점유하지 않도록 한다(원본이 정적 데이터이므로 read 일관성 문제 없음).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BulkReindexer {

    /** keyset 페이지 크기(브리프 명시값). 리컨실 델타 페이지도 동일 크기를 재사용한다. */
    public static final int PAGE_SIZE = 5000;

    private final EntityManager entityManager;
    private final ElasticsearchClient elasticsearchClient;
    private final SearchMetrics searchMetrics;

    /**
     * 모든 도메인을 전량 재색인하고 도메인별 색인 문서 수를 반환한다(관리 엔드포인트/초기 색인 요약용).
     */
    public Map<String, Long> reindexAll() {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (SearchDomain domain : SearchDomain.values()) {
            summary.put(domain.label(), reindex(domain));
        }
        return summary;
    }

    /**
     * 한 도메인 전체를 keyset 스트림으로 재색인한 뒤 인덱스를 refresh 하고 색인한 문서 수를 반환한다.
     */
    public long reindex(SearchDomain domain) {
        long lastId = 0;
        long total = 0;
        log.info("ES 전량 색인 시작: domain={}, index={}", domain.label(), domain.indexName());

        while (true) {
            List<Object[]> rows = fetchFullPage(domain, lastId);
            if (rows.isEmpty()) {
                break;
            }
            List<IndexedDoc> docs = new ArrayList<>(rows.size());
            for (Object[] row : rows) {
                SearchDomain.MappedRow mapped = domain.mapRow(row);
                docs.add(mapped.doc());
                lastId = mapped.doc().id();
            }
            bulkIndex(domain, docs);
            total += docs.size();
        }

        refresh(domain);
        log.info("ES 전량 색인 완료: domain={}, index={}, indexed={}", domain.label(), domain.indexName(), total);
        return total;
    }

    /**
     * 델타 페이지: {@code updated_at > since AND id > afterId} 를 id 오름차순으로 최대 limit 건 로드한다.
     */
    public List<SearchDomain.MappedRow> loadDelta(SearchDomain domain, Instant since, long afterId, int limit) {
        Query query = entityManager.createNativeQuery(domain.deltaSelectSql());
        query.setParameter("since", since);
        query.setParameter("last", afterId);
        query.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<SearchDomain.MappedRow> mapped = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            mapped.add(domain.mapRow(row));
        }
        return mapped;
    }

    /**
     * 주어진 id 들의 ES 문서 content_hash 를 mget 으로 조회한다(존재하지 않는 문서는 결과에서 빠짐).
     */
    public Map<Long, String> fetchContentHashes(SearchDomain domain, List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            return fetchContentHashesTyped(domain.indexName(), ids, domain.docType());
        } catch (IOException e) {
            throw new IllegalStateException("ES mget 실패: index=" + domain.indexName(), e);
        }
    }

    private <T extends IndexedDoc> Map<Long, String> fetchContentHashesTyped(
            String indexName, List<Long> ids, Class<T> type) throws IOException {
        List<String> idStrings = ids.stream().map(String::valueOf).toList();
        MgetResponse<T> response = elasticsearchClient.mget(m -> m.index(indexName).ids(idStrings), type);

        Map<Long, String> hashes = new HashMap<>();
        for (MultiGetResponseItem<T> item : response.docs()) {
            if (!item.isResult()) {
                continue;
            }
            GetResult<T> result = item.result();
            if (result.found() && result.source() != null) {
                hashes.put(Long.parseLong(result.id()), result.source().contentHash());
            }
        }
        return hashes;
    }

    /**
     * 문서 목록을 concrete 인덱스로 bulk 색인(op=index, _id=PK)한다.
     * bulk 응답의 개별 항목 실패를 검사해 ERROR 로그 + {@code search_bulk_item_failures_total} 로 표면화한다.
     */
    public void bulkIndex(SearchDomain domain, List<? extends IndexedDoc> docs) {
        if (docs.isEmpty()) {
            return;
        }
        String indexName = domain.indexName();
        BulkResponse response;
        try {
            response = elasticsearchClient.bulk(bulk -> {
                for (IndexedDoc doc : docs) {
                    bulk.operations(op -> op.index(idx -> idx
                            .index(indexName)
                            .id(String.valueOf(doc.id()))
                            .document(doc)));
                }
                return bulk;
            });
        } catch (IOException e) {
            throw new IllegalStateException("ES bulk 색인 실패: index=" + indexName, e);
        }

        if (response.errors()) {
            handleBulkErrors(indexName, response);
        }
    }

    private void handleBulkErrors(String indexName, BulkResponse response) {
        long failed = 0;
        for (BulkResponseItem item : response.items()) {
            if (item.error() == null) {
                continue;
            }
            failed++;
            searchMetrics.incrementBulkItemFailure();
            log.error("ES bulk 항목 실패: index={}, id={}, status={}, type={}, reason={}",
                    indexName, item.id(), item.status(), item.error().type(), item.error().reason());
        }
        log.error("ES bulk 부분 실패: index={}, failedItems={}", indexName, failed);
    }

    /** 색인 직후 count 가 즉시 보이도록 refresh 한다. */
    public void refresh(SearchDomain domain) {
        try {
            elasticsearchClient.indices().refresh(r -> r.index(domain.indexName()));
        } catch (IOException e) {
            throw new IllegalStateException("ES refresh 실패: index=" + domain.indexName(), e);
        }
    }

    /** alias 기준 ES 문서 수(드리프트 비교·초기 색인 skip 판단용). */
    public long esCount(SearchDomain domain) {
        try {
            return elasticsearchClient.count(c -> c.index(domain.aliasName())).count();
        } catch (IOException e) {
            throw new IllegalStateException("ES count 실패: alias=" + domain.aliasName(), e);
        }
    }

    /** PG 원본 테이블 행 수(드리프트 비교용). */
    public long pgCount(SearchDomain domain) {
        Object result = entityManager
                .createNativeQuery("SELECT count(*) FROM " + domain.tableName())
                .getSingleResult();
        return ((Number) result).longValue();
    }

    private List<Object[]> fetchFullPage(SearchDomain domain, long lastId) {
        Query query = entityManager.createNativeQuery(domain.fullSelectSql());
        query.setParameter("last", lastId);
        query.setParameter("limit", PAGE_SIZE);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows;
    }
}
