package org.wemightmove.movemap.domain.program.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.domain.program.dto.request.ProgramSearchByKeywordRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse.ProgramSimpleItem;
import org.wemightmove.movemap.global.search.index.ProgramDoc;
import org.wemightmove.movemap.global.search.index.SearchDomain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 프로그램 키워드 검색 ES 어댑터(primary).
 * <p>
 * 쿼리(GLOBAL §9 — 타입드 빌더만, {@code query_string}/{@code wildcard}/{@code regexp} 금지):
 * <pre>
 *   bool.should[ match(name, kw), match(name.ngram, kw), match(facility_name, kw) ].minimumShouldMatch(1)
 * </pre>
 * 정렬은 <b>id ASC 단일</b>(_score 정렬 안 함) — API 의 {@code nextCursor} 가 단일 id 이고 레거시 페이징이
 * id 오름차순이므로 그 계약을 보존한다. {@code search_after=[lastId]} 로 커서 페이징하며,
 * size+1 probe 로 {@code hasNext} 를 판정한다(레거시 DB 경로와 동일 시맨틱).
 * <p>
 * GLOBAL §2 — ES 문서 타입({@link ProgramDoc})은 이 어댑터 밖으로 새어나가지 않고 {@link ProgramSimpleItem} 로 매핑된다.
 */
@Component
@RequiredArgsConstructor
public class EsProgramSearchAdapter implements ProgramSearchPort {

    private static final String INDEX = SearchDomain.PROGRAM.aliasName();

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public ProgramSimpleListResponse search(ProgramSearchByKeywordRequest request) {
        String keyword = request.normalizedKeyword();
        int size = request.size();
        Long cursor = request.cursor();
        int probe = size + 1;

        SearchResponse<ProgramDoc> response;
        try {
            response = elasticsearchClient.search(s -> {
                s.index(INDEX)
                        .size(probe)
                        .query(q -> q.bool(b -> b
                                .should(sh -> sh.match(m -> m.field("name").query(keyword)))
                                .should(sh -> sh.match(m -> m.field("name.ngram").query(keyword)))
                                .should(sh -> sh.match(m -> m.field("facility_name").query(keyword)))
                                .minimumShouldMatch("1")))
                        .sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)));
                if (cursor != null) {
                    s.searchAfter(FieldValue.of(cursor.longValue()));
                }
                return s;
            }, ProgramDoc.class);
        } catch (IOException e) {
            throw new IllegalStateException("ES 프로그램 검색 실패: index=" + INDEX, e);
        }

        List<ProgramSimpleItem> items = new ArrayList<>(probe);
        for (Hit<ProgramDoc> hit : response.hits().hits()) {
            ProgramDoc doc = hit.source();
            if (doc == null) {
                continue;
            }
            items.add(new ProgramSimpleItem(
                    doc.id(),
                    doc.name(),
                    doc.facilityName(),
                    doc.facilitySubtype(),
                    doc.address()
            ));
        }

        // size+1 probe → hasNext 판정 후 size 로 trim (레거시 DB 경로와 동일)
        boolean hasNext = items.size() > size;
        List<ProgramSimpleItem> content = hasNext ? items.subList(0, size) : items;
        Long nextCursor = hasNext ? content.get(content.size() - 1).id() : null;

        return ProgramSimpleListResponse.of(content, nextCursor, hasNext);
    }
}
