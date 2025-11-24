package org.wemightmove.movemap.domain.member.dto.response;

import org.wemightmove.movemap.global.enums.FacilityType;

public record MemberProgramResponse(
        Long programId,
        String programName,
        FacilityType facilityType,
        Double averageRating,
        int reviewCount,
        Double distance,
        String address

) {
}
