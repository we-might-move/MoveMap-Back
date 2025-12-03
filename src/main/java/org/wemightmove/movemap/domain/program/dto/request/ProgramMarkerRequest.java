package org.wemightmove.movemap.domain.program.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 프로그램 마커 조회 Request
 * - 위치별 그룹핑하여 마커 표시
 */
@Schema(description = "프로그램 마커 조회 조건 요청", name = "programConditions")
public record ProgramMarkerRequest(
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

        String city,
        String district,

        @Min(value = 0)
        Integer minPrice,

        @Min(value = 0)
        Integer maxPrice,

        @Min(value = 0)
        @Max(value = 100)
        Integer minAge,

        @Min(value = 0)
        @Max(value = 100)
        Integer maxAge,

        @Min(value = 0)
        @Max(value = 100)
        Integer maxResults
) {
    public ProgramMarkerRequest {
        if (northEastLat != null && southWestLat != null && northEastLat <= southWestLat) {
            throw new IllegalArgumentException("northEastLat 는 southWestLat 보다 커야 합니다");
        }
        if (northEastLng != null && southWestLng != null && northEastLng <= southWestLng) {
            throw new IllegalArgumentException("northEastLng 는 southWestLng 보다 커야 합니다");
        }
        if (maxResults == null) {
            maxResults = 500;
        }
    }

    public boolean hasSearchConditions() {
        return (keyword != null && !keyword.isBlank()) ||
                (city != null && district != null) ||
                minPrice != null ||
                maxPrice != null ||
                minAge != null ||
                maxAge != null;
    }
}