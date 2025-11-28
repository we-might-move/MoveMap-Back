package org.wemightmove.movemap.domain.member.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.wemightmove.movemap.domain.member.dto.response.FavoriteProgramResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MemberProgramRepositoryCustomImpl implements MemberProgramRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<FavoriteProgramResponse> findFavoriteProgramsByMemberId(
            Long memberId,
            BigDecimal currentLatitude,
            BigDecimal currentLongitude,
            Long cursor,
            int size
    ) {
        String sql = """
            SELECT 
                mp.id AS member_program_id,
                p.id AS program_id,
                p.name,
                p.facility_type,
                p.facility_subtype,
                p.address,
                p.latitude,
                p.longitude,
                ST_Distance(
                    p.location::geography,
                    ST_SetSRID(ST_MakePoint(:currentLongitude, :currentLatitude), 4326)::geography
                ) AS distance_in_meters,
                p.hmpg_url,
                p.begin_date,
                p.end_date,
                p.weekday_number,
                p.price,
                p.start_time,
                p.end_time,
                p.target,
                p.capacity,
                COALESCE(AVG(pr.rating), 0.0) AS average_rating,
                COUNT(pr.id) AS review_count
            FROM member_program mp
            INNER JOIN program p ON mp.program_id = p.id
            LEFT JOIN program_review pr ON p.id = pr.program_id
            WHERE mp.member_id = :memberId
                AND (CAST(:cursor AS bigint) IS NULL OR mp.id > CAST(:cursor AS bigint))
            GROUP BY 
                mp.id,
                p.id,
                p.name,
                p.facility_type,
                p.facility_subtype,
                p.address,
                p.latitude,
                p.longitude,
                p.location,
                p.hmpg_url,
                p.begin_date,
                p.end_date,
                p.weekday_number,
                p.price,
                p.start_time,
                p.end_time,
                p.target,
                p.capacity
            ORDER BY mp.id DESC
            LIMIT :size
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("memberId", memberId);
        query.setParameter("currentLatitude", currentLatitude);
        query.setParameter("currentLongitude", currentLongitude);
        query.setParameter("cursor", cursor);
        query.setParameter("size", size);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(this::mapToFavoriteProgramResponse)
                .collect(Collectors.toList());
    }

    private FavoriteProgramResponse mapToFavoriteProgramResponse(Object[] row) {
        int idx = 0;
        return new FavoriteProgramResponse(
                ((Number) row[idx++]).longValue(),
                ((Number) row[idx++]).longValue(),
                (String) row[idx++],
                (String) row[idx++],
                (String) row[idx++],
                (String) row[idx++],
                (BigDecimal) row[idx++],
                (BigDecimal) row[idx++],
                row[idx++] != null ? ((Number) row[idx-1]).doubleValue() : null,
                (String) row[idx++],
                row[idx++] != null ? ((java.sql.Date) row[idx-1]).toLocalDate() : null,
                row[idx++] != null ? ((java.sql.Date) row[idx-1]).toLocalDate() : null,
                row[idx++] != null ? ((Number) row[idx-1]).intValue() : null,
                row[idx++] != null ? ((Number) row[idx-1]).intValue() : null,
                row[idx++] != null ? ((java.sql.Time) row[idx-1]).toLocalTime() : null,
                row[idx++] != null ? ((java.sql.Time) row[idx-1]).toLocalTime() : null,
                row[idx++] != null ? ((Number) row[idx-1]).intValue() : null,
                row[idx++] != null ? ((Number) row[idx-1]).intValue() : null,
                row[idx++] != null ? ((Number) row[idx-1]).doubleValue() : 0.0,
                ((Number) row[idx]).longValue()
        );
    }
}
