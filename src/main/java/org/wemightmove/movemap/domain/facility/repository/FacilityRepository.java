package org.wemightmove.movemap.domain.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.facility.entity.Facility;

public interface FacilityRepository extends JpaRepository<Facility, Long>, FacilityRepositoryCustom {
}
