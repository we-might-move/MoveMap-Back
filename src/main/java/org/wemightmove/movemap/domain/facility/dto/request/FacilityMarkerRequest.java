package org.wemightmove.movemap.domain.facility.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 시설 마커 조회 Request (Viewport + 검색)
 */
public record FacilityMarkerRequest(
        // ===== Viewport 좌표 (필수) =====
        @NotNull(message = "북동쪽 위도는 필수입니다")
        @DecimalMax(value = "43.0", message = "대한민국 위도는 43 이하여야 합니다")
        @DecimalMin(value = "33.0", message = "대한민국 위도는 33 이상이어야 합니다")
        Double northEastLat,

        @DefaultValue("132.0")
        @NotNull(message = "북동쪽 경도는 필수입니다")
        @DecimalMax(value = "132.0", message = "대한민국 경도는 132 이하여야 합니다")
        @DecimalMin(value = "124.0", message = "대한민국 경도는 124 이상이어야 합니다")
        Double northEastLng,

        @NotNull(message = "남서쪽 위도는 필수입니다")
        @DecimalMin(value = "33.0", message = "대한민국 위도는 33 이상이어야 합니다")
        @DecimalMax(value = "43.0", message = "대한민국 위도는 43 이하여야 합니다")
        Double southWestLat,

        @NotNull(message = "남서쪽 경도는 필수입니다")
        @DecimalMin(value = "124.0", message = "대한민국 경도는 124 이상이어야 합니다")
        @DecimalMax(value = "132.0", message = "대한민국 경도는 132 이하여야 합니다")
        Double southWestLng,

        @Size(max = 50, message = "검색어는 50자 이하여야 합니다")
        String keyword,

        String city,
        String district,
//        List<FacilityType> facilityTypes,
        Boolean isVoucherAvailable,

        @Min(value = 1, message = "최대 결과 수는 1 이상이어야 합니다")
        @Max(value = 1000, message = "최대 결과 수는 1000 이하여야 합니다")
        Integer maxResults
) {
    public FacilityMarkerRequest {
        // Viewport 검증
        if (northEastLat != null && southWestLat != null && northEastLat <= southWestLat) {
            throw new IllegalArgumentException("northEastLat 는 southWestLat 보다 커야 합니다");
        }
        if (northEastLng != null && southWestLng != null && northEastLng <= southWestLng) {
            throw new IllegalArgumentException("northEastLng 는 southWestLng 보다 커야 합니다");
        }

        // 기본값 설정
        if (maxResults == null || maxResults <= 0) {
            maxResults = 500;
        }
        if (maxResults > 1000) {
            maxResults = 1000;
        }
    }

    /**
     * 검색 조건이 있는지 확인
     */
    public boolean hasSearchConditions() {
        return (keyword != null && !keyword.isBlank()) ||
                (city != null && district != null) || // 지역구 단위로 찾으므로
                isVoucherAvailable != null;
    }
}