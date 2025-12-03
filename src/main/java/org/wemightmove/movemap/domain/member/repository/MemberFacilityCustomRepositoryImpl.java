package org.wemightmove.movemap.domain.member.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.wemightmove.movemap.domain.member.dto.response.FavoriteFacilityResponse;
import org.wemightmove.movemap.domain.member.entity.QMemberFacility;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberFacilityCustomRepositoryImpl implements MemberFacilityCustomRepository {

    private final EntityManager entityManager;

    @Override
    public List<FavoriteFacilityResponse> findFavoriteFacilityWithDistance(Long memberId, BigDecimal currentLatitude, BigDecimal currentLongitude, Long cursor, int size) {

        String sql = """
            SELECT 
                mf.id,
                f.id,
                f.name,
                f.facility_subtype,
                f.latitude,
                f.longitude,
                f.address,
                COALESCE(AVG(fr.rating), 0.0) as average_rating,
                CAST(COUNT(fr.id) AS bigint) as review_count,
                ST_Distance(
                    f.location::geography,
                    ST_MakePoint(:longitude, :latitude)::geography
                ) as distance
            FROM member_facility mf
            INNER JOIN facility f ON mf.facility_id = f.id
            LEFT JOIN facility_review fr ON fr.facility_id = f.id
            WHERE mf.member_id = :memberId 
              AND (CAST(:cursor AS bigint) IS NULL OR mf.id < CAST(:cursor AS bigint))
            GROUP BY 
                mf.id,
                f.id,
                f.name,
                f.facility_subtype,
                f.latitude,
                f.longitude,
                f.address,
                f.location
            ORDER BY mf.id DESC
            LIMIT :size
            """;

        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("memberId", memberId)
                .setParameter("latitude", currentLatitude)
                .setParameter("longitude", currentLongitude)
                .setParameter("cursor", cursor)
                .setParameter("size", size)
                .getResultList();

        return results.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<FavoriteFacilityResponse> findFavoriteFacility(Long memberId, Long cursor, int size) {

        String sql = """
            SELECT 
                mf.id,
                f.id,
                f.name,
                f.facility_subtype,
                f.latitude,
                f.longitude,
                f.address,
                COALESCE(AVG(fr.rating), 0.0) as average_rating,
                CAST(COUNT(fr.id) AS bigint) as review_count,
                NULL as distance
            FROM member_facility mf
            INNER JOIN facility f ON mf.facility_id = f.id
            LEFT JOIN facility_review fr ON fr.facility_id = f.id
            WHERE mf.member_id = :memberId 
              AND (CAST(:cursor AS bigint) IS NULL OR mf.id < CAST(:cursor AS bigint))
            GROUP BY 
                mf.id,
                f.id,
                f.name,
                f.facility_subtype,
                f.latitude,
                f.longitude,
                f.address,
                f.location
            ORDER BY mf.id DESC
            LIMIT :size
            """;

        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("memberId", memberId)
                .setParameter("cursor", cursor)
                .setParameter("size", size)
                .getResultList();

        return results.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private FavoriteFacilityResponse mapToResponse(Object[] row) {
        return new FavoriteFacilityResponse(
                ((Number) row[0]).longValue(),      // memberFacilityId
                ((Number) row[1]).longValue(),      // facilityId
                (String) row[2],                    // name
                (String) row[3],                    // facilitySubtype
                (BigDecimal) row[4],                // latitude
                (BigDecimal) row[5],                // longitude
                (String) row[6],                    // address
                ((Number) row[7]).doubleValue(),    // averageRating
                ((Number) row[8]).longValue(),      // reviewCount
                row[9] != null ? ((Number) row[9]).doubleValue() : null // distance (meters)
        );
    }

    private BooleanExpression cursorCondition(Long cursor, QMemberFacility memberFacility) {
        return cursor != null ? memberFacility.id.lt(cursor) : null;
    }
}
