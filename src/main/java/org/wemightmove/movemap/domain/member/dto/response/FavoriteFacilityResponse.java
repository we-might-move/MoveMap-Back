package org.wemightmove.movemap.domain.member.dto.response;

import java.math.BigDecimal;

public record FavoriteFacilityResponse(
        Long memberFacilityId, // cursor 로 사용
        Long facilityId,
        String facilityName,
        String facilitySubtype,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        Double averageRating,
        Long reviewCount,
        Double distance
) {
    /**
     * 거리를 킬로미터로 변환
     * @return 거리 (km)
     */
    public Double getDistanceInKm() {
        return distance != null ? distance / 1000.0 : null;
    }

    /**
     * 거리를 포맷팅된 문자열로 반환
     * @return "1.2km" 또는 "350m" 형식
     */
    public String getFormattedDistance() {
        if (distance == null) return "거리 정보 없음";

        if (distance >= 1000) {
            return String.format("%.1fkm", distance / 1000.0);
        } else {
            return String.format("%.0fm", distance);
        }
    }
}
