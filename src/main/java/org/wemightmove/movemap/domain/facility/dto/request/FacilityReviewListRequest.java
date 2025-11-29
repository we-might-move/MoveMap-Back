package org.wemightmove.movemap.domain.facility.dto.request;

import java.math.BigDecimal;

public record FacilityReviewListRequest(
        // 위치 기반 검색
        BigDecimal latitude,
        BigDecimal longitude,

        // 지역 기반 검색
        String city,
        String district,

        // 검색 키워드
        String keyword,

        // 페이지네이션
        Long cursor,
        Integer size
) {
    public FacilityReviewListRequest {
        // validation
        if (latitude == null && (city == null && district == null)) {
            throw new IllegalArgumentException("현재 위치 또는 지역구 입력 중 하나는 필수입니다.");
        }

        if (cursor != null && cursor <= 0) {
            cursor = null;
        }

        if (size == null || size <= 0) {
            size = 20;
        }
    }

    public boolean isLocationBased() {
        return latitude != null && longitude != null;
    }
}