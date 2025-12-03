package org.wemightmove.movemap.domain.league.dto.response;

import lombok.Builder;

@Builder
public record RegionLeagueResponse(
        Long regionId,
        String regionName,
        String leagueType,
        String leagueColorCode
) {}