package org.wemightmove.movemap.domain.facility.dto.response;

import java.math.BigDecimal;

/**
 * FIXME: 왜 ReviewItem 내부에 cursor가 존재하는지?
 * @param facilityId
 * @param facilityName
 * @param facilityType
 * @param address
 * @param latitude
 * @param longitude
 * @param distance
 * @param reviewId
 * @param rating
 * @param reviewTitle
 * @param reviewContent
 * @param reviewerNickname
 * @param cursor
 */
public record FacilityReviewItem(
        Long facilityId,
        String facilityName,
        String facilityType,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Double distance, // 거리 기반 검색 시에만

        Long reviewId,
        Integer rating,
        String reviewTitle,
        String reviewContent,
        String reviewerNickname,

        // 페이지네이션용
        Long cursor
) {
    public static FacilityReviewItem of(
            Long facilityId,
            String facilityName,
            String facilityType,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            Double distance,
            Long reviewId,
            Integer rating,
            String reviewTitle,
            String reviewContent,
            String reviewerNickname
    ) {
        return new FacilityReviewItem(
                facilityId,
                facilityName,
                facilityType,
                address,
                latitude,
                longitude,
                distance,
                reviewId,
                rating,
                reviewTitle,
                reviewContent,
                reviewerNickname,
                reviewId // cursor는 reviewId 사용
        );
    }
}
