package org.wemightmove.movemap.domain.league.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record LeagueRankResponse(
        int year,
        int weekNumber,
        List<LeagueRankUnitResponse> ranks
) {}