package org.wemightmove.movemap.domain.program.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.wemightmove.movemap.domain.program.dto.request.ProgramMarkerRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProgramRepositoryCustomImpl implements ProgramRepositoryCustom {

    private static final double DEFAULT_RADIUS_METERS = 5000.0; // 반경 1km
    private final EntityManager entityManager;

    @Override
    public List<ProgramMarkerResponse.MarkerInfo> findMakersByRegionCode(BigDecimal lat, BigDecimal lng, int maxResults, Long memberId) {
        String sql = """
            SELECT
                p.latitude,
                p.longitude,
                COUNT(p.id) as program_count,
                MIN(p.name) as representative_name,
                CASE WHEN COUNT(mp.id) > 0 THEN true ELSE false END as has_bookmark
            FROM program p
            LEFT JOIN member_program mp ON p.id = mp.program_id AND mp.member_id = :memberId
            WHERE ST_DWithin(
                p.location::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                :radius
            )
            GROUP BY p.latitude, p.longitude
            ORDER BY MIN(
                ST_Distance(
                    p.location::geography,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
                )
            ) ASC
            LIMIT :maxResults
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("lat", lat);
        query.setParameter("lng", lng);
        query.setParameter("radius", DEFAULT_RADIUS_METERS);
        query.setParameter("memberId", memberId);
        query.setParameter("maxResults", maxResults);

        return executeProgramMarkerQuery(query);
    }

    private List<ProgramMarkerResponse.MarkerInfo> executeProgramMarkerQuery(Query query) {
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    BigDecimal latitude = (BigDecimal) row[0];
                    BigDecimal longitude = (BigDecimal) row[1];
                    int programCount = ((Number) row[2]).intValue();
                    String representativeName = (String) row[3];
                    boolean hasBookmark = (Boolean) row[4];

                    return new ProgramMarkerResponse.MarkerInfo(
                            latitude,
                            longitude,
                            programCount,
                            representativeName,
                            hasBookmark
                    );
                })
                .collect(Collectors.toList());
    }
}
