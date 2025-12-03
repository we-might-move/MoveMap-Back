package org.wemightmove.movemap.domain.league.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.league.dto.response.LeagueRankResponse;
import org.wemightmove.movemap.domain.league.dto.response.LeagueRankUnitResponse;
import org.wemightmove.movemap.domain.league.entity.LeagueStatus;
import org.wemightmove.movemap.domain.league.entity.WeeklyRegionScore;
import org.wemightmove.movemap.domain.league.repository.LeagueStatusRepository;
import org.wemightmove.movemap.domain.league.repository.WeeklyRegionScoreRepository;
import org.wemightmove.movemap.global.entity.RegionType;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeagueServiceImpl implements LeagueService {

    private final WeeklyRegionScoreRepository weeklyRegionScoreRepository;
    private final LeagueStatusRepository leagueStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public LeagueRankResponse getCurrentWeekRanking() {

        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.KOREA);
        int year = today.getYear();
        int weekNumber = today.get(weekFields.weekOfWeekBasedYear());

        // 1. 이번 주에 점수가 존재하는 자치구들의 점수 맵 (regionId -> score)
        List<WeeklyRegionScore> weeklyScores =
                weeklyRegionScoreRepository.findByYearAndWeekNumberOrderByScoreDesc(year, weekNumber);

        Map<Long, Integer> scoreByRegionId = weeklyScores.stream()
                .collect(Collectors.toMap(
                        ws -> ws.getRegion().getId(),
                        WeeklyRegionScore::getScore
                ));

        // 2. 기준은 LeagueStatus: 리그에 속한 모든 자치구를 대상으로 순위 생성
        List<LeagueStatus> allStatuses = leagueStatusRepository.findAll();

        // 3. 각 자치구별로 weeklyScore를 채우되, 없으면 0점 처리
        List<LeagueRankUnitResponse> list = new ArrayList<>();

        for (LeagueStatus status : allStatuses) {
            RegionType region = status.getRegion();
            int weeklyScore = scoreByRegionId.getOrDefault(region.getId(), 0);

            list.add(LeagueRankUnitResponse.builder()
                    .regionId(region.getId())
                    .regionName(region.getName())
                    .weeklyScore(weeklyScore)
                    .leagueType(status.getType().getName())
                    .leagueColorCode(status.getType().getColorCode())
                    .rank(0) // 일단 0으로 넣고, 나중에 정렬 후 순위 매김
                    .build());
        }

        // 4. 점수 기준으로 정렬 (동점이면 regionName 기준 등 추가로 정렬 조건 줄 수 있음)
        list.sort(Comparator
                .comparingInt(LeagueRankUnitResponse::weeklyScore)
                .reversed()
                .thenComparing(LeagueRankUnitResponse::regionName));

        // 5. rank 부여
        List<LeagueRankUnitResponse> ranked = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            LeagueRankUnitResponse u = list.get(i);
            ranked.add(new LeagueRankUnitResponse(
                    u.regionId(),
                    u.regionName(),
                    u.weeklyScore(),
                    u.leagueType(),
                    u.leagueColorCode(),
                    i + 1
            ));
        }

        return LeagueRankResponse.builder()
                .year(year)
                .weekNumber(weekNumber)
                .ranks(ranked)
                .build();
    }
}