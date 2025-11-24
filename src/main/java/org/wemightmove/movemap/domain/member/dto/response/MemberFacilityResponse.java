package org.wemightmove.movemap.domain.member.dto.response;

import org.wemightmove.movemap.global.enums.FacilityType;

public record MemberFacilityResponse(
        Long facilityId,
        String facilityName,
        FacilityType facilityType,
        Double averageRating,
        int reviewCount,
        Double distance,
        String address

) {
}
