package org.wemightmove.movemap.domain.facility.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.domain.facility.dto.response.FacilitySimpleListResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilitySimpleListResponse.FacilitySimpleInfo;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.search.index.FacilityDoc;
import org.wemightmove.movemap.global.search.index.SearchDomain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 시설 키워드 검색 ES 어댑터(primary).
 * <p>
 * 쿼리(GLOBAL §9 — 타입드 빌더만)는 <b>AND-first, graceful OR-fallback</b> 2단 구성이다:
 * <pre>
 *   1) AND(cross_fields): multi_match(fields=[name, facility_subtype], type=cross_fields, operator=AND)
 *      — 다중 토큰 쿼리를 "각 필드 통틀어 모든 토큰이 매치"로 해석한다. name.ngram/facility_subtype.ngram
 *        은 analyzer 가 달라 cross_fields 대상에서 제외(부분매칭은 폴백 쪽 몫).
 *   2) AND 결과가 0건이면 기존 OR 쿼리로 폴백(변경 없음):
 *      bool.should[ match(name, kw), match(name.ngram, kw),
 *                   match(facility_subtype, kw), match(facility_subtype.ngram, kw) ].minimumShouldMatch(1)
 * </pre>
 * {@code facility_subtype} 는 레거시 {@code facility_subtype LIKE '%kw%'}(부분매칭)를 미러링하기 위해
 * 분석 필드(nori + edge_ngram)로 매칭한다({@code name} 과 동형). 이렇게 하지 않으면 "골프" 검색 시
 * 골프연습장/파크골프장 같은 하위유형이 누락된다.
 * {@code size=30} 고정, 커서 없음(레거시 시설 검색과 동일) — AND·OR 두 쿼리 모두 동일하게 적용된다.
 * 키워드는 raw(정규화 안 함).
 * <p>
 * GLOBAL §2 — ES 문서 타입({@link FacilityDoc})은 밖으로 새어나가지 않고 {@link FacilitySimpleInfo} 로 매핑된다.
 * 색인된 {@code facility_type}(enum STRING, 예: {@code BALL_GAME})은 DTO 계약과 동일하게 {@link FacilityType#getName()}
 * (한글 표기)로 변환한다({@link FacilitySimpleInfo#of} 파리티).
 */
@Component
@RequiredArgsConstructor
public class EsFacilitySearchAdapter {

    private static final String INDEX = SearchDomain.FACILITY.aliasName();
    private static final int SIZE = 30;

    private final ElasticsearchClient elasticsearchClient;

    public FacilitySimpleListResponse search(String keyword) {
        SearchResponse<FacilityDoc> response = executeSearch(keyword, true);
        if (response.hits().hits().isEmpty()) {
            // AND(cross_fields) 가 0건이면 기존 OR 쿼리로 폴백 — happy path(AND 매치 존재)에서는
            // 2차 ES 호출이 발생하지 않는다.
            response = executeSearch(keyword, false);
        }

        List<FacilitySimpleInfo> facilities = new ArrayList<>(SIZE);
        for (Hit<FacilityDoc> hit : response.hits().hits()) {
            FacilityDoc doc = hit.source();
            if (doc == null) {
                continue;
            }
            facilities.add(new FacilitySimpleInfo(
                    doc.id(),
                    doc.name(),
                    FacilityType.from(doc.facilityType()).getName(),
                    doc.facilitySubtype(),
                    doc.address()
            ));
        }

        return new FacilitySimpleListResponse(facilities);
    }

    /**
     * {@code andFirst=true} 면 AND(cross_fields) 쿼리를, {@code false} 면 기존 OR 쿼리를 실행한다.
     * size(=30 고정)는 두 분기 모두 동일하게 적용된다(레거시 계약과 동일 — 커서 없음).
     */
    private SearchResponse<FacilityDoc> executeSearch(String keyword, boolean andFirst) {
        try {
            return elasticsearchClient.search(s -> {
                s.index(INDEX).size(SIZE);
                if (andFirst) {
                    s.query(q -> q.multiMatch(mm -> mm
                            .fields("name", "facility_subtype")
                            .query(keyword)
                            .type(TextQueryType.CrossFields)
                            .operator(Operator.And)));
                } else {
                    s.query(q -> q.bool(b -> b
                            .should(sh -> sh.match(m -> m.field("name").query(keyword)))
                            .should(sh -> sh.match(m -> m.field("name.ngram").query(keyword)))
                            .should(sh -> sh.match(m -> m.field("facility_subtype").query(keyword)))
                            .should(sh -> sh.match(m -> m.field("facility_subtype.ngram").query(keyword)))
                            .minimumShouldMatch("1")));
                }
                return s;
            }, FacilityDoc.class);
        } catch (IOException e) {
            throw new IllegalStateException("ES 시설 검색 실패: index=" + INDEX, e);
        }
    }
}
