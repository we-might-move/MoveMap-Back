package org.wemightmove.movemap.domain.member.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record FavoriteProgramResponse(
        Long memberProgramId,
        Long programId,
        String name,
        String facilityType,
        String facilitySubtype,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Double distanceInMeters,
        String hmpgUrl,
        LocalDate beginDate,
        LocalDate endDate,
        Integer weekdayNumber,
        Integer price,
        LocalTime startTime,
        LocalTime endTime,
        Integer target,
        Integer capacity,
        Double averageRating,
        Long reviewCount
) {
    public FavoriteProgramResponse {
        if (memberProgramId == null || programId == null) {
            throw new IllegalArgumentException("memberProgramId and programId cannot be null");
        }
    }
}