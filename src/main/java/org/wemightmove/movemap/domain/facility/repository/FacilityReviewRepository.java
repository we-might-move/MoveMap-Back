package org.wemightmove.movemap.domain.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.domain.facility.entity.FacilityReview;
import org.wemightmove.movemap.domain.member.entity.Member;

import java.math.BigDecimal;
import java.util.List;

public interface FacilityReviewRepository extends JpaRepository<FacilityReview, Long> {
    boolean existsFacilityReviewByMemberAndFacility(Member member, Facility facility);
    /**
     * 위치 기반 리뷰 리스트 조회 (거리순 정렬)
     * - 사용자 위치 기준 가까운 시설의 리뷰부터 조회
     * - ST_Distance로 거리 계산 (단위: 미터)
     * - cursor 기반 페이지네이션
     */
    @Query(value = """
        SELECT 
            f.id as facilityId,
            f.name as facilityName,
            f.facility_type as facilityType,
            f.address as address,
            f.latitude as latitude,
            f.longitude as longitude,
            ST_Distance(
                f.location::geography,
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
            ) as distance,
            fr.id as reviewId,
            fr.rating as rating,
            fr.content as reviewContent,
            m.nickname as reviewerNickname
        FROM facility_review fr
        INNER JOIN facility f ON fr.facility_id = f.id
        INNER JOIN member m ON fr.member_id = m.id
        WHERE 
            (:keyword IS NULL OR (
                f.name ILIKE CONCAT('%', CAST(:keyword AS text), '%') OR
                f.address ILIKE CONCAT('%', CAST(:keyword AS text), '%')
            ))
            AND (:cursor IS NULL OR fr.id < :cursor)
        ORDER BY distance ASC, fr.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findReviewsByLocation(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("keyword") String keyword,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );

    /**
     * 지역 기반 리뷰 리스트 조회 (최신순 정렬)
     * - regionCode로 필터링
     * - 최신 리뷰부터 조회
     * - cursor 기반 페이지네이션
     */
    @Query(value = """
        SELECT 
            f.id as facilityId,
            f.name as facilityName,
            f.facility_type as facilityType,
            f.address as address,
            f.latitude as latitude,
            f.longitude as longitude,
            NULL as distance,
            fr.id as reviewId,
            fr.rating as rating,
            fr.content as reviewContent,
            m.nickname as reviewerNickname
        FROM facility_review fr
        INNER JOIN facility f ON fr.facility_id = f.id
        INNER JOIN member m ON fr.member_id = m.id
        WHERE 
            f.region_cd LIKE CONCAT(CAST(:regionCode AS text), '%')
            AND (:keyword IS NULL OR (
                f.name ILIKE CONCAT('%', CAST(:keyword AS text), '%') OR
                f.address ILIKE CONCAT('%', CAST(:keyword AS text), '%')
            ))
            AND (:cursor IS NULL OR fr.id < :cursor)
        ORDER BY fr.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findReviewsByRegion(
            @Param("regionCode") String regionCode,
            @Param("keyword") String keyword,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );
}
