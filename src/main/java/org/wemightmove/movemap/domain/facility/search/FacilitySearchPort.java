package org.wemightmove.movemap.domain.facility.search;

import org.wemightmove.movemap.domain.facility.dto.response.FacilitySimpleListResponse;

/**
 * 시설 키워드 검색 포트(엔진 추상화).
 * <p>
 * 기존 {@code FacilityQueryService#searchFacilityListByKeyword} 의 검색 계약을 미러링한다:
 * 입력은 raw keyword(레거시 시설 검색은 정규화하지 않았다), 출력은 {@link FacilitySimpleListResponse}.
 * 구현체는 ES({@link EsFacilitySearchAdapter}) 와 DB({@link DbFacilitySearchAdapter}) 두 가지이며,
 * feature flag({@code movemap.search.facility.engine}) 와 fallback 은 서비스 레이어(라우터)가 담당한다.
 * <p>
 * GLOBAL CONSTRAINT §2 — ES 문서 타입은 이 포트 밖으로 새어나가지 않는다(어댑터에서 기존 DTO 로 매핑).
 */
public interface FacilitySearchPort {

    FacilitySimpleListResponse search(String keyword);
}
