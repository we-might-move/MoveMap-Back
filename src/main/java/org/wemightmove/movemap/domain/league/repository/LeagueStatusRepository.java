package org.wemightmove.movemap.domain.league.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.league.entity.LeagueStatus;
import org.wemightmove.movemap.global.entity.RegionType;

import java.util.Optional;

public interface LeagueStatusRepository extends JpaRepository<LeagueStatus, Long> {
    Optional<LeagueStatus> findByRegion(RegionType region);
}
