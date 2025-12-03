package org.wemightmove.movemap.domain.league.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wemightmove.movemap.domain.league.dto.response.LeagueRankResponse;
import org.wemightmove.movemap.domain.league.service.LeagueService;

@RestController
@Tag(name = "League")
@RequiredArgsConstructor
@RequestMapping("/league")
public class LeagueController {

    private final LeagueService leagueService;

    @Operation(summary = "주간 리그 순위 조회", description = "리그별 주간 지역구 순위를 조회합니다.")
    @GetMapping()
    public LeagueRankResponse getCurrentRank() {
        return leagueService.getCurrentWeekRanking();
    }
}
