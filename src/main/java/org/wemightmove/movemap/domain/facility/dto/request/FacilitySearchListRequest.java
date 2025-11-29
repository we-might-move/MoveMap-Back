package org.wemightmove.movemap.domain.facility.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 뷰포트 기반 시설 리스트 조회 요청
 */
public record FacilitySearchListRequest(
        @Schema(description = "북동쪽 위도", example = "43.0")
        @NotNull(message = "북동쪽 위도는 필수입니다")
        @DecimalMax(value = "43.0", message = "대한민국 위도는 43 이하여야 합니다")
        @DecimalMin(value = "33.0", message = "대한민국 위도는 33 이상이어야 합니다")
        Double northEastLat,

        @Schema(description = "북동쪽 경도", example = "132.0")
        @NotNull(message = "북동쪽 경도는 필수입니다")
        @DecimalMax(value = "132.0", message = "대한민국 경도는 132 이하여야 합니다")
        @DecimalMin(value = "124.0", message = "대한민국 경도는 124 이상이어야 합니다")
        Double northEastLng,

        @Schema(description = "남서쪽 위도", example = "33.0")
        @NotNull(message = "남서쪽 위도는 필수입니다")
        @DecimalMin(value = "33.0", message = "대한민국 위도는 33 이상이어야 합니다")
        @DecimalMax(value = "43.0", message = "대한민국 위도는 43 이하여야 합니다")
        Double southWestLat,

        @Schema(description = "남서쪽 경도", example = "124.0")
        @NotNull(message = "남서쪽 경도는 필수입니다")
        @DecimalMin(value = "124.0", message = "대한민국 경도는 124 이상이어야 합니다")
        @DecimalMax(value = "132.0", message = "대한민국 경도는 132 이하여야 합니다")
        Double southWestLng,

        @Schema(description = "검색 키워드 (시설명)", example = "축구")
        @Size(max = 50, message = "검색어는 50자 이하여야 합니다")
        String keyword,

        @Schema(description = "시/도 이름", example = "서울특별시")
        @Size(max = 20, message = "시/도 이름은 20자 이하여야 합니다")
        String city,

        @Schema(description = "시/군/구 이름", example = "강남구")
        @Size(max = 20, message = "시/군/구 이름은 20자 이하여야 합니다")
        String district,

        @Schema(description = "바우처 이용 가능 여부", example = "true")
        Boolean isVoucherAvailable,

        @Schema(description = "커서 (마지막 조회 시설 ID, 첫 페이지는 null 또는 생략)")
        Long cursor,

        @Schema(description = "페이지 크기 (기본값: 20, 최대: 100)", example = "20")
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size
) {
    /**
     * Compact constructor - Validation 만 수행 (값 변경 불가)
     */
    public FacilitySearchListRequest {
        // ✅ null 체크 후 validation
        if (northEastLat != null && southWestLat != null) {
            if (northEastLat <= southWestLat) {
                throw new IllegalArgumentException(
                        "northEastLat(" + northEastLat + ")는 southWestLat(" + southWestLat + ")보다 커야 합니다"
                );
            }
        }

        if (northEastLng != null && southWestLng != null) {
            if (northEastLng <= southWestLng) {
                throw new IllegalArgumentException(
                        "northEastLng(" + northEastLng + ")는 southWestLng(" + southWestLng + ")보다 커야 합니다"
                );
            }
        }

        // ✅ city 와 district 는 함께 제공되어야 함
        if ((city != null && district == null) || (city == null && district != null)) {
            throw new IllegalArgumentException(
                    "city 와 district 는 함께 제공되어야 합니다"
            );
        }
    }

    /**
     * 정규화된 커서 값 반환
     * - null 또는 0 이하면 null 반환 (첫 페이지)
     */
    public Long normalizedCursor() {
        if (cursor == null || cursor <= 0) {
            return null;
        }
        return cursor;
    }

    /**
     * 커서가 유효한지 확인
     */
    public boolean hasCursor() {
        return normalizedCursor() != null;
    }

    /**
     * 검색 조건이 있는지 확인
     */
    public boolean hasSearchConditions() {
        return (keyword != null && !keyword.isBlank()) ||
                (city != null && district != null) ||
                (isVoucherAvailable != null && isVoucherAvailable);
    }
}