package org.wemightmove.movemap.domain.league.dto.response;

import lombok.Builder;

@Builder
public record LeagueRankUnitResponse(
        Long regionId,
        String regionName,
        int weeklyScore,
        String leagueType,
        String leagueColorCode,
        int rank
) {}