package org.wemightmove.movemap.domain.facility.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
 * 쿼리(GLOBAL §9 — 타입드 빌더만):
 * <pre>
 *   bool.should[ match(name, kw), match(name.ngram, kw), term(facility_subtype, kw) ].minimumShouldMatch(1)
 * </pre>
 * {@code size=30} 고정, 커서 없음(레거시 시설 검색과 동일). 키워드는 raw(정규화 안 함).
 * <p>
 * GLOBAL §2 — ES 문서 타입({@link FacilityDoc})은 밖으로 새어나가지 않고 {@link FacilitySimpleInfo} 로 매핑된다.
 * 색인된 {@code facility_type}(enum STRING, 예: {@code BALL_GAME})은 DTO 계약과 동일하게 {@link FacilityType#getName()}
 * (한글 표기)로 변환한다({@link FacilitySimpleInfo#of} 파리티).
 */
@Component
@RequiredArgsConstructor
public class EsFacilitySearchAdapter implements FacilitySearchPort {

    private static final String INDEX = SearchDomain.FACILITY.aliasName();
    private static final int SIZE = 30;

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public FacilitySimpleListResponse search(String keyword) {
        SearchResponse<FacilityDoc> response;
        try {
            response = elasticsearchClient.search(s -> s
                    .index(INDEX)
                    .size(SIZE)
                    .query(q -> q.bool(b -> b
                            .should(sh -> sh.match(m -> m.field("name").query(keyword)))
                            .should(sh -> sh.match(m -> m.field("name.ngram").query(keyword)))
                            .should(sh -> sh.term(t -> t.field("facility_subtype").value(keyword)))
                            .minimumShouldMatch("1"))), FacilityDoc.class);
        } catch (IOException e) {
            throw new IllegalStateException("ES 시설 검색 실패: index=" + INDEX, e);
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
}
