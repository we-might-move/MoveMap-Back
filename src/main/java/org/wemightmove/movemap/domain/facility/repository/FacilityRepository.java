package org.wemightmove.movemap.domain.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.global.entity.RegionType;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Long>, FacilityRepositoryCustom {
    @Query(value = "SELECT * FROM facility f WHERE f.name LIKE CONCAT('%', :keyword, '%') OR f.facility_subtype LIKE CONCAT('%', :keyword, '%') LIMIT 30", nativeQuery = true)
    List<Facility> searchFacilitiesByNameAndFacilitySubtype(@Param("keyword") String keyword);


}
