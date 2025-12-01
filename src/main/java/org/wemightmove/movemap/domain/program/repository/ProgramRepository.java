package org.wemightmove.movemap.domain.program.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.program.dto.ProgramDetailProjection;
import org.wemightmove.movemap.domain.program.entity.Program;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long>, ProgramRepositoryCustom {

    /**
     * 프로그램 상세 정보 단일 쿼리 조회
     *
     * - 리뷰 통계 / 북마크 여부 / 사용자와의 거리까지 한 번에 조회
     * - 결과는 Projection으로 받고, 상위 레이어에서 DTO로 변환
     */
    @Query(value = """
            SELECT 
                p.id AS id,
                p.name AS name,
                p.facility_type AS facilityType,
                p.facility_subtype AS facilitySubtype,
                p.latitude AS latitude,
                p.longitude AS longitude,
                p.address AS address,
                p.hmpg_url AS hmpgUrl,
                p.begin_date AS beginDate,
                p.end_date AS endDate,
                p.weekday_number AS weekdayNumber,
                p.price::int AS price,
                p.start_time AS startTime,
                p.end_time AS endTime,
                p.target AS target,
                p.capacity AS capacity,
                CASE 
                    WHEN :userLatitude IS NOT NULL AND :userLongitude IS NOT NULL
                    THEN ROUND(
                        CAST(
                            ST_Distance(
                                p.location::geography,
                                ST_SetSRID(ST_MakePoint(:userLongitude, :userLatitude), 4326)::geography
                            ) / 1000.0 AS numeric
                        ), 2
                    )
                    ELSE NULL
                END AS distance,
                COALESCE(ROUND(AVG(pr.rating)::numeric, 1), 0.0) AS avgRating,
                COALESCE(COUNT(pr.id), 0)::int AS reviewCount,
                CASE 
                    WHEN :memberId IS NOT NULL 
                    THEN EXISTS(
                        SELECT 1 
                        FROM member_program mp 
                        WHERE mp.program_id = p.id 
                        AND mp.member_id = :memberId
                    )
                    ELSE false
                END AS isBookmarked
            FROM program p
            LEFT JOIN program_review pr ON pr.program_id = p.id
            WHERE p.id = :programId
            GROUP BY p.id, p.name, p.facility_type, p.facility_subtype,
                     p.latitude, p.longitude, p.address, p.hmpg_url,
                     p.begin_date, p.end_date, p.weekday_number, p.price,
                     p.start_time, p.end_time, p.target, p.capacity
            """, nativeQuery = true)
    Optional<ProgramDetailProjection> findProgramDetailById(
            @Param("programId") Long programId,
            @Param("memberId") Long memberId,
            @Param("userLatitude") BigDecimal userLatitude,
            @Param("userLongitude") BigDecimal userLongitude
    );
}
