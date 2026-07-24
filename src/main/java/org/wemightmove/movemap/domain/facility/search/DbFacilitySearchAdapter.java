package org.wemightmove.movemap.domain.facility.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.domain.facility.dto.response.FacilitySimpleListResponse;
import org.wemightmove.movemap.domain.facility.repository.FacilityRepository;

import java.util.List;

/**
 * 시설 키워드 검색 DB 어댑터.
 * <p>
 * 기존 {@code FacilityQueryServiceImpl#searchFacilityListByKeyword} 에 있던
 * {@code facilityRepository.searchFacilitiesByNameAndFacilitySubtype} 경로를 바이트 동일하게 옮겨온 것이다
 * ({@code name LIKE %kw% OR facility_subtype LIKE %kw% LIMIT 30}). 커서/hasNext 없음.
 */
@Component
@RequiredArgsConstructor
public class DbFacilitySearchAdapter {

    private final FacilityRepository facilityRepository;

    public FacilitySimpleListResponse search(String keyword) {
        List<FacilitySimpleListResponse.FacilitySimpleInfo> facilities =
                facilityRepository.searchFacilitiesByNameAndFacilitySubtype(keyword).stream()
                        .map(FacilitySimpleListResponse.FacilitySimpleInfo::of).toList();

        return new FacilitySimpleListResponse(facilities);
    }
}
