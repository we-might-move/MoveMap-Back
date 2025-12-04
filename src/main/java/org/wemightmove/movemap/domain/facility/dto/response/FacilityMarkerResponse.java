package org.wemightmove.movemap.domain.facility.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * 시설 마커 조회 Response
 * - 최소한의 정보만 포함 (지도 표시용)
 * - 무한 스크롤 없음
 */
public record FacilityMarkerResponse(
        List<MarkerInfo> markers,
        int totalCount,
        boolean hasSearchConditions              // 검색 조건 적용 여부
) {
    public record MarkerInfo(
            Long facilityId,
            BigDecimal latitude,
            BigDecimal longitude,
            String facilityType,                 // 마커 아이콘 구분용
            String name                          // 마커 클릭 시 미리보기용
    ) {}
}