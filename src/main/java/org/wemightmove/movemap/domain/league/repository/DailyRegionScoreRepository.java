package org.wemightmove.movemap.domain.league.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.league.entity.DailyRegionScore;
import org.wemightmove.movemap.global.entity.RegionType;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyRegionScoreRepository extends CrudRepository<DailyRegionScore, Long> {

    Optional<DailyRegionScore> findByRegionAndDate(RegionType region, LocalDate date);

    @Query("""
           select coalesce(sum(d.score), 0)
           from DailyRegionScore d
           where d.region = :region
             and d.date between :start and :end
           """)
    int sumScoreByRegionAndDateBetween(@Param("region") RegionType region,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);
}
