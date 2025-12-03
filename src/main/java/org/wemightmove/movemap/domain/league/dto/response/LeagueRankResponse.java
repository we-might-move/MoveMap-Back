package org.wemightmove.movemap.domain.league.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record LeagueRankResponse(
        int year,
        int month,
        int weekNumber,
        Map<String, List<LeagueRankUnitResponse>> ranks
) {}