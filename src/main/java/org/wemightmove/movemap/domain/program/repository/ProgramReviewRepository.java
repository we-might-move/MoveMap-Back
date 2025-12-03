package org.wemightmove.movemap.domain.program.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.program.dto.ProgramReviewProjection;
import org.wemightmove.movemap.domain.program.entity.ProgramReview;

import java.util.List;

public interface ProgramReviewRepository extends JpaRepository<ProgramReview, Long> {
    /**
     * 특정 회원이 특정 프로그램에 작성한 리뷰 존재 여부 확인
     */
    boolean existsByMemberIdAndProgramId(Long memberId, Long programId);

    @Query(value = """
            SELECT 
                pr.id AS reviewId,
                m.id AS memberId,
                m.nickname AS memberNickname,
                p.id AS programId,
                p.name AS programName,
                pr.rating AS rating,
                pr.title AS title,
                COALESCE(f.name, p.name) AS facilityName,
                COALESCE(f.facility_subtype, p.facility_subtype) AS facilitySubtype,
                pr.content AS content
            FROM program_review pr
            INNER JOIN member m ON pr.member_id = m.id
            INNER JOIN program p ON pr.program_id = p.id
            LEFT JOIN facility f ON 
                ST_DWithin(
                    p.location::geography,
                    f.location::geography,
                    100
                )
            WHERE 1=1
                AND m.is_deleted = false
                AND (:cursor IS NULL OR pr.id < :cursor)
                AND (:regionCode IS NULL OR p.region_cd LIKE CONCAT(:regionCode, '%'))
                AND (
                    :keyword IS NULL 
                    OR pr.title LIKE CONCAT('%', :keyword, '%')
                    OR p.name LIKE CONCAT('%', :keyword, '%')
                    OR COALESCE(f.name, p.name) LIKE CONCAT('%', :keyword, '%')
                    OR pr.content LIKE CONCAT('%', :keyword, '%')
                )
            ORDER BY pr.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ProgramReviewProjection> findProgramReviewsWithCursor(
            @Param("cursor") Long cursor,
            @Param("regionCode") String regionCode,
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );
}
