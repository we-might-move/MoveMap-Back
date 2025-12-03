package org.wemightmove.movemap.domain.league.repository;

import org.springframework.data.repository.CrudRepository;
import org.wemightmove.movemap.domain.league.entity.WeeklyRegionScore;
import org.wemightmove.movemap.global.entity.RegionType;

import java.util.List;
import java.util.Optional;

public interface WeeklyRegionScoreRepository extends CrudRepository<WeeklyRegionScore, Long> {

    Optional<WeeklyRegionScore> findByRegionAndYearAndWeekNumber(RegionType region, int year, int weekNumber);

    List<WeeklyRegionScore> findByYearAndWeekNumberOrderByScoreDesc(int year, int weekNumber);

}
