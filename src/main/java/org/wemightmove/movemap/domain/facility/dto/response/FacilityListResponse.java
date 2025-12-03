package org.wemightmove.movemap.domain.facility.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public record FacilityListResponse(
        @Schema(description = "시설 리스트")
        List<FacilityInfo> facilities,

        @Schema(description = "다음 커서 (다음 페이지 요청 시 사용)")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {
    public static FacilityListResponse of(List<FacilityInfo> facilities, Long nextCursor, boolean hasNext) {
        return new FacilityListResponse(facilities, nextCursor, hasNext);
    }

    @Schema(description = "시설 정보")
    public record FacilityInfo(
            @Schema(description = "시설 ID", example = "1")
            Long id,

            @Schema(description = "시설명", example = "올림픽공원 테니스장")
            String name,

            @Schema(description = "위도", example = "37.5219")
            BigDecimal latitude,

            @Schema(description = "경도", example = "127.1230")
            BigDecimal longitude,

            @Schema(description = "시설 타입", example = "BALL_GAME")
            String facilityType,

            @Schema(description = "시설 서브타입", example = "테니스")
            String facilitySubtype,

            @Schema(description = "주소", example = "서울특별시 송파구 올림픽로 424")
            String address,

            @Schema(description = "바우처 이용 가능 여부", example = "true")
            boolean isVoucherAvailable,

            @Schema(description = "사용자로부터 거리 (미터)", example = "1234.56")
            Double distanceMeters,

            @Schema(description = "평균 평점", example = "4.5")
            Double avgRating,

            @Schema(description = "리뷰 개수", example = "42")
            Long reviewCount,

            @Schema(description = "즐겨찾기 여부", example = "true")
            boolean isBookmarked
    ) { }
}