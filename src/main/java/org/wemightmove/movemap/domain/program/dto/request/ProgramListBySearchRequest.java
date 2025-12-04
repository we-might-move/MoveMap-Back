package org.wemightmove.movemap.domain.program.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.time.LocalDate;
import java.util.List;
/**
 * 프로그램 리스트 조회 Request (뷰포트 + 필터링)
 */
@Schema(description = "프로그램 리스트 필터링 조회 요청")
public record ProgramListBySearchRequest(
        @Schema(description = "북동쪽 위도", example = "37.5665")
        @NotNull(message = "북동쪽 위도는 필수입니다")
        @DecimalMax(value = "43.0", message = "위도는 43 이하여야 합니다")
        @DecimalMin(value = "33.0", message = "위도는 33 이상이어야 합니다")
        Double northEastLat,

        @Schema(description = "북동쪽 경도", example = "127.0")
        @NotNull(message = "북동쪽 경도는 필수입니다")
        @DecimalMax(value = "132.0", message = "경도는 132 이하여야 합니다")
        @DecimalMin(value = "124.0", message = "경도는 124 이상이어야 합니다")
        Double northEastLng,

        @Schema(description = "남서쪽 위도", example = "37.5000")
        @NotNull(message = "남서쪽 위도는 필수입니다")
        @DecimalMin(value = "33.0", message = "위도는 33 이상이어야 합니다")
        @DecimalMax(value = "43.0", message = "위도는 43 이하여야 합니다")
        Double southWestLat,

        @Schema(description = "남서쪽 경도", example = "126.9")
        @NotNull(message = "남서쪽 경도는 필수입니다")
        @DecimalMin(value = "124.0", message = "경도는 124 이상이어야 합니다")
        @DecimalMax(value = "132.0", message = "경도는 132 이하여야 합니다")
        Double southWestLng,

        @Schema(description = "사용자 현재 위도 (거리 계산용)", example = "37.5665")
        Double userLat,

        @Schema(description = "사용자 현재 경도 (거리 계산용)", example = "126.9780")
        Double userLng,

        @Schema(description = "검색 키워드", example = "축구")
        @Size(max = 50, message = "검색어는 50자 이하여야 합니다")
        String keyword,

        @Schema(description = "시/도", example = "서울특별시")
        String city,

        @Schema(description = "시/군/구", example = "강남구")
        String district,

       @Schema(description = "최소 가격", example = "0")
       @Min(value = 0, message = "최소 가격은 0 이상이어야 합니다")
       Integer minPrice,

       @Schema(description = "최대 가격", example = "50000")
       @Min(value = 0, message = "최대 가격은 0 이상이어야 합니다")
       Integer maxPrice,

       @Schema(description = "최소 나이", example = "7")
       @Min(value = 0, message = "최소 나이는 0 이상이어야 합니다")
       @Max(value = 100, message = "최소 나이는 100 이하여야 합니다")
       Integer minAge,

       @Schema(description = "최대 나이", example = "13")
       @Min(value = 0, message = "최대 나이는 0 이상이어야 합니다")
       @Max(value = 100, message = "최대 나이는 100 이하여야 합니다")
       Integer maxAge,

      @Schema(description = "시작 날짜 필터", example = "2025-01-01")
       LocalDate startDate,

      @Schema(description = "종료 날짜 필터", example = "2025-12-31")
      LocalDate endDate,

      @Schema(description = "커서 (마지막 조회한 프로그램 ID 또는 거리)", example = "100")
      Long cursor,

      @Schema(description = "페이지 크기", example = "20")
      @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
      @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
      Integer size
) {
        public ProgramListBySearchRequest {
            if (northEastLat != null && southWestLat != null && northEastLat <= southWestLat) {
                throw new IllegalArgumentException("northEastLat는 southWestLat보다 커야 합니다");
            }
            if (northEastLng != null && southWestLng != null && northEastLng <= southWestLng) {
                throw new IllegalArgumentException("northEastLng는 southWestLng보다 커야 합니다");
            }
            if (size == null || size <= 0) {
                size = 20;
            }
            if (size > 100) {
                size = 100;
            }
        }

        public boolean hasSearchConditions() {
            return (keyword != null && !keyword.isBlank()) ||
                    (city != null && district != null) ||
                    minPrice != null ||
                    maxPrice != null ||
                    minAge != null ||
                    maxAge != null ||
                    startDate != null ||
                    endDate != null;
        }

        public boolean isFreeSearch() {
            return minPrice != null && minPrice == 0 && maxPrice != null && maxPrice == 0;
        }
}

