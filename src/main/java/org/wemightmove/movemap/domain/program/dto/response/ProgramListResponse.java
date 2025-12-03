package org.wemightmove.movemap.domain.program.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.util.AgeGroupUtil;
import org.wemightmove.movemap.global.util.WeekdayUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "프로그램 리스트 응답")
public record ProgramListResponse(
        @Schema(description = "프로그램 목록")
        List<ProgramItem> programs,

        @Schema(description = "다음 커서 (다음 페이지 조회용)", example = "50")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "현재 페이지 아이템 개수", example = "20")
        int currentSize
)
{

    public static ProgramListResponse of(
            List<ProgramItem> programs,
            Long nextCursor,
            boolean hasNext
    ) {
        return new ProgramListResponse(
                programs,
                nextCursor,
                hasNext,
                programs.size()
        );
    }

    @Schema(description = "프로그램 상세 정보")
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

            @Schema(description = "운영 요일 목록", example = "[\"월\", \"화\", \"수\", \"목\", \"금\"]")
            String[] weekdays,

            @Schema(description = "가격", example = "30000")
            Integer price,

            @Schema(description = "시작 시간", example = "14:00")
            @JsonFormat(pattern = "HH:mm")
            LocalTime startTime,

            @Schema(description = "종료 시간", example = "16:00")
            @JsonFormat(pattern = "HH:mm")
            LocalTime endTime,

            @Schema(description = "대상 연령 범위", example = "[\"초등 1-2학년\", \"초등 3-4학년\", \"초등 5-6학년\"]")
            String[] targetAgeGroups,

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
        public static ProgramItem mapToDTO(Object[] row) {
            return new ProgramItem(
                    ((Number) row[0]).longValue(),                                              // id
                    (String) row[1],                                                            // name
                    FacilityType.valueOf((String) row[2]),                                      // facilityType
                    (String) row[3],                                                            // facilitySubtype
                    (BigDecimal) row[4],                                                        // latitude
                    (BigDecimal) row[5],                                                        // longitude
                    (String) row[6],                                                            // address
                    (String) row[7],                                                            // hmpgUrl
                    row[8] != null ? ((java.sql.Date) row[8]).toLocalDate() : null,           // beginDate
                    row[9] != null ? ((java.sql.Date) row[9]).toLocalDate() : null,         // endDate
                    row[10] != null ? WeekdayUtil.decodeWeekdays(((Number) row[10]).intValue()) : null,                   // weekdayNumber
                    row[11] != null ? ((Number) row[11]).intValue() : null,                   // price
                    row[12] != null ? ((java.sql.Time) row[12]).toLocalTime() : null,         // startTime
                    row[13] != null ? ((java.sql.Time) row[13]).toLocalTime() : null,         // endTime
                    row[14] != null ? AgeGroupUtil.decodeAgeGroups(((Number) row[14]).intValue()) : null,                   // target
                    row[15] != null ? ((Number) row[15]).intValue() : null,                   // capacity
                    row[16] != null ? ((Number) row[16]).doubleValue() : null,                // distance
                    row[17] != null ? ((Number) row[17]).doubleValue() : 0.0,                 // avgRating
                    row[18] != null ? ((Number) row[18]).intValue() : 0,                      // reviewCount
                    (Boolean) row[19]                                                          // isBookmarked
            );
        }
    }
}
