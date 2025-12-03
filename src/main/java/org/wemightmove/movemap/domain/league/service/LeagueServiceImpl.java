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
import org.wemightmove.movemap.global.enums.LeagueType;

import java.time.DayOfWeek;
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

        int year = today.getYear();
        int month = today.getMonthValue();
        int weekNumber = today.get(WeekFields.of(DayOfWeek.MONDAY, 4).weekOfMonth());

        // regionId → score (없으면 0)
        List<WeeklyRegionScore> weeklyScores =
                weeklyRegionScoreRepository.findByYearAndMonthAndWeekNumber(year, month, weekNumber);

        Map<Long, Integer> scores = weeklyScores.stream()
                .collect(Collectors.toMap(
                        w -> w.getRegion().getId(),
                        WeeklyRegionScore::getScore
                ));

        // 모든 자치구 기준
        List<LeagueStatus> allStatuses = leagueStatusRepository.findAll();

        // 리그 타입별 그룹 Map 초기화
        Map<String, List<LeagueRankUnitResponse>> result = new HashMap<>();
        for (LeagueType type : LeagueType.values()) {
            result.put(type.toString(), new ArrayList<>());
        }

        // DTO 변환
        List<LeagueRankUnitResponse> allRankDtos = new ArrayList<>();

        for (LeagueStatus s : allStatuses) {
            RegionType region = s.getRegion();
            LeagueType type = s.getType();

            int score = scores.getOrDefault(region.getId(), 0);

            allRankDtos.add(new LeagueRankUnitResponse(
                    region.getId(),
                    buildRegionFullName(region),
                    score,
                    0
            ));
        }

        // 점수 기준 정렬 + 동점 순위 처리 (1,1,3)
        allRankDtos.sort(Comparator.comparingInt(LeagueRankUnitResponse::weeklyScore).reversed());

        int currentRank = 1;
        int actualRank = 1;
        Integer lastScore = null;

        List<LeagueRankUnitResponse> ranked = new ArrayList<>();

        for (LeagueRankUnitResponse dto : allRankDtos) {
            if (lastScore != null && dto.weeklyScore() != lastScore) {
                actualRank = currentRank;
            }

            ranked.add(new LeagueRankUnitResponse(
                    dto.regionId(),
                    dto.regionName(),
                    dto.weeklyScore(),
                    actualRank
            ));

            lastScore = dto.weeklyScore();
            currentRank++;
        }

        // 리그 타입별로 분리해서 담기
        for (LeagueRankUnitResponse dto : ranked) {
            LeagueType type = leagueStatusRepository.findByRegion_Id(dto.regionId())
                    .orElseThrow()
                    .getType();
            result.get(type.toString()).add(dto);
        }

        return new LeagueRankResponse(year, month, weekNumber, result);
    }

    private String buildRegionFullName(RegionType region) {
        Deque<String> names = new ArrayDeque<>();
        RegionType current = region;

        while (current != null) {
            names.addFirst(current.getName());
            current = current.getParent();
        }

        return String.join(" ", names); // "서울특별시 강남구"
    }
}