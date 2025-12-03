package org.wemightmove.movemap.domain.league.service;

import org.wemightmove.movemap.domain.league.dto.response.LeagueRankResponse;
import org.wemightmove.movemap.domain.league.dto.response.RegionLeagueResponse;

public interface LeagueService {

    public LeagueRankResponse getCurrentWeekRanking();
    public RegionLeagueResponse getMyRegionLeague();
}
