package org.wemightmove.movemap.domain.facility.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FacilityRepositoryCustomImpl implements FacilityRepositoryCustom {

    private static final double DEFAULT_RADIUS_METERS = 5000.0;

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public List<FacilityMarkerResponse.MarkerInfo> findMakersByRegionCode(BigDecimal lat, BigDecimal lng, int maxResults) {

        // ✅ Native SQL 직접 사용 (Hibernate 파서 완전 우회)
        String sql = """
            SELECT 
                f.id,
                f.latitude,
                f.longitude,
                f.facility_type,
                f.name
            FROM facility f
            WHERE ST_DWithin(
                f.location::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                :radius
            )
            ORDER BY ST_Distance(
                f.location::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
            ) ASC
            LIMIT %d
            """.formatted(maxResults);


        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("lat", lat);
        query.setParameter("lng", lng);
        query.setParameter("radius", DEFAULT_RADIUS_METERS);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new FacilityMarkerResponse.MarkerInfo(
                        ((Number) row[0]).longValue(),           // id
                        (BigDecimal) row[1],                     // latitude
                        (BigDecimal) row[2],                     // longitude
                        ((String) row[3]),                       // facility_type
                        (String) row[4]                          // name
                ))
                .collect(Collectors.toList());
    }
}
