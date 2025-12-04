package org.wemightmove.movemap.global.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.wemightmove.movemap.global.entity.RegionType;

import java.util.Optional;

@Repository
public interface RegionTypeRepository extends JpaRepository<RegionType, Long> {

    @Query("SELECT r FROM RegionType r WHERE r.prefix = :prefix")
    Optional<RegionType> findParentRegionTypeByPrefix(@Param("prefix") String prefix);

    @Query("SELECT r FROM RegionType r WHERE r.prefix LIKE CONCAT(:prefix, '%')")
    Optional<RegionType> findChildRegionTypeByPrefix(@Param("prefix") String prefix);

    @Query("SELECT r FROM RegionType r WHERE r.name = :name and r.parent.name = :parentName")
    Optional<RegionType> findRegionByNameAndParentName(@Param("name") String name, @Param("parentName") String parentName);

    @Query("SELECT r FROM RegionType r WHERE r.name = :name")
    Optional<RegionType> findRegionByName(@Param("name") String name);
}
