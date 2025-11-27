package org.wemightmove.movemap.domain.record.dto.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record MonthDailyFlagsResponse(
        int year,
        int month,
        Map<Integer, Boolean> flags
) {

}
