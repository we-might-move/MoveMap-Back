package org.wemightmove.movemap.domain.league.dto.response;

import lombok.Builder;

@Builder
public record LeagueRankUnitResponse(
        Long regionId,
        String regionName,
        int weeklyScore,
        int rank
) {}