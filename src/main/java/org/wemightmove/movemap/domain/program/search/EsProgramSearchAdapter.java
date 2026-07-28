package org.wemightmove.movemap.domain.program.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
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
 * 쿼리(GLOBAL §9 — 타입드 빌더만, {@code query_string}/{@code wildcard}/{@code regexp} 금지)는
 * <b>AND-first, graceful OR-fallback</b> 2단 구성이다:
 * <pre>
 *   1) AND(cross_fields): multi_match(fields=[name, facility_name], type=cross_fields, operator=AND)
 *      — 다중 토큰 쿼리("강남 축구")를 "각 필드 통틀어 모든 토큰이 매치"로 해석해
 *        "강남"과 "축구"가 서로 다른 필드(facility_name/name)에 있어도 함께 요구한다.
 *        name.ngram 은 analyzer 가 달라 cross_fields 대상에서 제외(부분매칭은 폴백 쪽 몫).
 *   2) AND 결과가 0건이면 기존 OR 쿼리로 폴백(변경 없음):
 *      bool.should[ match(name, kw), match(name.ngram, kw), match(facility_name, kw) ].minimumShouldMatch(1)
 * </pre>
 * 임계값 0(=완전 미스일 때만 폴백)을 쓰는 이유: 어떤 검색이든 매치가 있으면 항상 같은 분기(AND 또는
 * OR)로 판정되어야 커서 페이징이 페이지마다 흔들리지 않는다 — {@code search_after} 는 이미 매치된
 * 결과셋 안에서 커서 이후만 건너뛸 뿐 매치 여부 자체를 바꾸지 않으므로, 총 매치 0건은 어떤 커서를
 * 넣어도 항상 0건이다(키워드별로 AND/OR 분기가 불변).
 * <p>
 * 정렬은 <b>id ASC 단일</b>(_score 정렬 안 함) — API 의 {@code nextCursor} 가 단일 id 이고 레거시 페이징이
 * id 오름차순이므로 그 계약을 보존한다. {@code search_after=[lastId]} 로 커서 페이징하며,
 * size+1 probe 로 {@code hasNext} 를 판정한다(레거시 DB 경로와 동일 시맨틱). 정렬/커서/probe 는
 * AND·OR 두 쿼리 모두 동일하게 적용된다.
 * <p>
 * GLOBAL §2 — ES 문서 타입({@link ProgramDoc})은 이 어댑터 밖으로 새어나가지 않고 {@link ProgramSimpleItem} 로 매핑된다.
 */
@Component
@RequiredArgsConstructor
public class EsProgramSearchAdapter {

    private static final String INDEX = SearchDomain.PROGRAM.aliasName();

    private final ElasticsearchClient elasticsearchClient;

    public ProgramSimpleListResponse search(ProgramSearchByKeywordRequest request) {
        String keyword = request.normalizedKeyword();
        int size = request.size();
        Long cursor = request.cursor();
        int probe = size + 1;

        SearchResponse<ProgramDoc> response = executeSearch(keyword, probe, cursor, true);
        if (response.hits().hits().isEmpty()) {
            // AND(cross_fields) 가 0건이면 기존 OR 쿼리로 폴백 — happy path(AND 매치 존재)에서는
            // 2차 ES 호출이 발생하지 않는다.
            response = executeSearch(keyword, probe, cursor, false);
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

    /**
     * {@code andFirst=true} 면 AND(cross_fields) 쿼리를, {@code false} 면 기존 OR 쿼리를 실행한다.
     * 정렬(id ASC)·{@code search_after} 커서·probe size 는 두 분기 모두 동일하게 적용해 페이징 계약을 보존한다.
     */
    private SearchResponse<ProgramDoc> executeSearch(String keyword, int probe, Long cursor, boolean andFirst) {
        try {
            return elasticsearchClient.search(s -> {
                s.index(INDEX).size(probe);
                if (andFirst) {
                    s.query(q -> q.multiMatch(mm -> mm
                            .fields("name", "facility_name")
                            .query(keyword)
                            .type(TextQueryType.CrossFields)
                            .operator(Operator.And)));
                } else {
                    s.query(q -> q.bool(b -> b
                            .should(sh -> sh.match(m -> m.field("name").query(keyword)))
                            .should(sh -> sh.match(m -> m.field("name.ngram").query(keyword)))
                            .should(sh -> sh.match(m -> m.field("facility_name").query(keyword)))
                            .minimumShouldMatch("1")));
                }
                s.sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)));
                if (cursor != null) {
                    s.searchAfter(FieldValue.of(cursor.longValue()));
                }
                return s;
            }, ProgramDoc.class);
        } catch (IOException e) {
            throw new IllegalStateException("ES 프로그램 검색 실패: index=" + INDEX, e);
        }
    }
}
