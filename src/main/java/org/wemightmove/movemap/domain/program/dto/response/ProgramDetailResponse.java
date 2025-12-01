package org.wemightmove.movemap.domain.program.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.wemightmove.movemap.domain.program.dto.ProgramItem;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.util.AgeGroupUtil;
import org.wemightmove.movemap.global.util.WeekdayUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "프로그램 상세 정보 응답")
public record ProgramDetailResponse(
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
    /**
     * ProgramItem을 ProgramDetailResponse로 변환
     * 비트마스크를 사람이 읽을 수 있는 형태로 디코딩
     */
    public static ProgramDetailResponse from(ProgramItem item) {
        return new ProgramDetailResponse(
                item.id(),
                item.name(),
                item.facilityType(),
                item.facilitySubtype(),
                item.latitude(),
                item.longitude(),
                item.address(),
                item.hmpgUrl(),
                item.beginDate(),
                item.endDate(),
                WeekdayUtil.decodeWeekdays(item.weekdayNumber()),
                item.price(),
                item.startTime(),
                item.endTime(),
                AgeGroupUtil.decodeAgeGroups(item.target()),
                item.capacity(),
                item.distance(),
                item.avgRating(),
                item.reviewCount(),
                item.isBookmarked()
        );
    }
}