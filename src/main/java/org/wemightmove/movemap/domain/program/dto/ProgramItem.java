package org.wemightmove.movemap.domain.program.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "프로그램 상세 정보 (DB 조회용)")
public record ProgramItem(
        @Schema(description = "프로그램 ID", example = "1")
        Long id,

        @Schema(description = "프로그램명", example = "청소년 축구 교실")
        String name,

        @Schema(description = "시설 타입", example = "BALL_GAME")
        FacilityType facilityType,

        @Schema(description = "시설 세부 타입", example = "축구장")
        String facilitySubtype,

        @Schema(description = "위도", example = "37.5665")
        BigDecimal latitude,

        @Schema(description = "경도", example = "126.9780")
        BigDecimal longitude,

        @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
        String address,

        @Schema(description = "홈페이지 URL", example = "https://example.com")
        String hmpgUrl,

        @Schema(description = "시작 날짜", example = "2025-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate beginDate,

        @Schema(description = "종료 날짜", example = "2025-12-31")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,

        @Schema(description = "요일 번호 (비트마스크)", example = "62")
        Integer weekdayNumber,

        @Schema(description = "가격", example = "30000")
        Integer price,

        @Schema(description = "시작 시간", example = "14:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @Schema(description = "종료 시간", example = "16:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,

        @Schema(description = "대상 연령 (비트마스크)", example = "255")
        Integer target,

        @Schema(description = "정원", example = "20")
        Integer capacity,

        @Schema(description = "사용자로부터의 거리 (km)", example = "2.5")
        Double distance,

        @Schema(description = "평균 평점", example = "4.5")
        Double avgRating,

        @Schema(description = "리뷰 개수", example = "15")
        Integer reviewCount,

        @Schema(description = "북마크 여부", example = "true")
        Boolean isBookmarked
) {
        /**
         * Native Query Projection → 실제 DTO로 변환
         */
        public static ProgramItem from(ProgramDetailProjection p) {
                return new ProgramItem(
                        p.getId(),
                        p.getName(),
                        FacilityType.valueOf(p.getFacilityType()),  // String → Enum
                        p.getFacilitySubtype(),
                        p.getLatitude(),
                        p.getLongitude(),
                        p.getAddress(),
                        p.getHmpgUrl(),
                        p.getBeginDate(),
                        p.getEndDate(),
                        p.getWeekdayNumber(),
                        p.getPrice(),
                        p.getStartTime(),
                        p.getEndTime(),
                        p.getTarget(),
                        p.getCapacity(),
                        p.getDistance() != null ? p.getDistance().doubleValue() : null,
                        p.getAvgRating().doubleValue(),
                        p.getReviewCount(),
                        p.getIsBookmarked()
                );
        }
}
