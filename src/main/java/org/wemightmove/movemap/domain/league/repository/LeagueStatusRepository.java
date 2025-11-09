package org.wemightmove.movemap.domain.league.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.league.entity.LeagueStatus;

public interface LeagueStatusRepository extends JpaRepository<LeagueStatus, Long> {
}
