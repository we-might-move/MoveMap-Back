package org.wemightmove.movemap.domain.facility.repository;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.facility.dto.FacilityInfoProjection;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.global.entity.RegionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FacilityRepository extends JpaRepository<Facility, Long>, FacilityRepositoryCustom {
    @Query(value = "SELECT * FROM facility f WHERE f.name LIKE CONCAT('%', :keyword, '%') OR f.facility_subtype LIKE CONCAT('%', :keyword, '%') LIMIT 30", nativeQuery = true)
    List<Facility> searchFacilitiesByNameAndFacilitySubtype(@Param("keyword") String keyword);

    @Query(value = """
    SELECT 
        f.id as id,
        f.name as name,
        f.latitude as latitude,
        f.longitude as longitude,
        f.facility_type as facilityType,
        f.facility_subtype as facilitySubtype,
        f.address as address,
        f.is_voucher_available as isVoucherAvailable,
        ST_Distance(
            f.location::geography,
            ST_SetSRID(ST_MakePoint(:memberLongitude, :memberLatitude), 4326)::geography
        ) as distanceMeters,
        COALESCE(AVG(fr.rating), 0.0) as avgRating,
        COALESCE(COUNT(fr.id), 0) as reviewCount,
        EXISTS(
            SELECT 1 
            FROM member_facility mf 
            WHERE mf.facility_id = f.id 
            AND mf.member_id = :memberId
        ) as isBookmarked
    FROM facility f
    LEFT JOIN facility_review fr ON f.id = fr.facility_id
    WHERE f.id = :facilityId
    GROUP BY f.id, f.name, f.latitude, f.longitude, f.facility_type, 
             f.facility_subtype, f.address, f.is_voucher_available, f.location
    """, nativeQuery = true)
    Optional<FacilityInfoProjection> findFacilityInfoWithLocation(
            @Param("facilityId") Long facilityId,
            @Param("memberId") Long memberId,
            @Param("memberLatitude") BigDecimal memberLatitude,
            @Param("memberLongitude") BigDecimal memberLongitude
    );
}
