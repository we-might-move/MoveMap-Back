package org.wemightmove.movemap.domain.facility.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityMarkerRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FacilityRepositoryCustomImpl implements FacilityRepositoryCustom {

    private static final double DEFAULT_RADIUS_METERS = 1000.0;

    private final EntityManager entityManager;

    @Override
    public List<FacilityMarkerResponse.MarkerInfo> findMakersByRegionCode(BigDecimal lat, BigDecimal lng, int maxResults) {

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

    @Override
    public List<FacilityMarkerResponse.MarkerInfo> findMarkersByViewport(FacilityMarkerRequest request, String regionCode, List<FacilityType> facilityTypes) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                f.id,
                f.latitude,
                f.longitude,
                f.facility_type,
                f.name
            FROM facility f
            WHERE 1=1
            """);

        // Viewport bbox 조건 추가
        sql.append("""
                AND f.latitude BETWEEN :southWestLat AND :northEastLat
                AND f.longitude BETWEEN :southWestLng AND :northEastLng
            """);

        // 검색 조건 동적 추가
        List<String> conditions = new ArrayList<>();

        if (request.keyword() != null && !request.keyword().isBlank()) {
            conditions.add("f.name ILIKE :keyword");
        }

        if (regionCode != null) {
            conditions.add("f.region_cd LIKE :regionCode || '%'");
        }

        if (facilityTypes != null && !facilityTypes.isEmpty()) {
            conditions.add("f.facility_type = ANY(:facilityTypes)");
        }

        if (request.isVoucherAvailable() != null && request.isVoucherAvailable()) {
            conditions.add("f.is_voucher_available = true");
        }

        // WHERE 절에 조건 추가
        if (!conditions.isEmpty()) {
            sql.append(" AND ").append(String.join(" AND ", conditions));
        }

        // 정렬: 검색 조건 있으면 최신순, 없으면 거리순
        if (request.hasSearchConditions()) {
            sql.append(" ORDER BY f.id DESC");
        } else {
            // Viewport 중심점 계산
            double centerLat = (request.northEastLat() + request.southWestLat()) / 2;
            double centerLng = (request.northEastLng() + request.southWestLng()) / 2;

            sql.append("""
                ORDER BY ST_DistanceSphere(
                    f.location,
                    ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326)
                ) ASC
                """);
        }

        sql.append(" LIMIT ").append(request.maxResults());

        // ✅ Query 생성 및 파라미터 바인딩
        Query query = entityManager.createNativeQuery(sql.toString());

        // Viewport 파라미터
        query.setParameter("northEastLat", request.northEastLat());
        query.setParameter("northEastLng", request.northEastLng());
        query.setParameter("southWestLat", request.southWestLat());
        query.setParameter("southWestLng", request.southWestLng());

        // 검색 조건 파라미터
        if (request.keyword() != null && !request.keyword().isBlank()) {
            query.setParameter("keyword", "%" + request.keyword() + "%");
        }

        if (regionCode != null) {
            query.setParameter("regionCode", regionCode);
        }

        if (facilityTypes != null && !facilityTypes.isEmpty()) {
            String[] typeArray = facilityTypes.stream()
                    .map(Enum::name)
                    .toArray(String[]::new);
            query.setParameter("facilityTypes", typeArray);
        }

        // 거리순 정렬용 중심점 (검색 조건 없을 때만)
        if (!request.hasSearchConditions()) {
            double centerLat = (request.northEastLat() + request.southWestLat()) / 2;
            double centerLng = (request.northEastLng() + request.southWestLng()) / 2;
            query.setParameter("centerLat", centerLat);
            query.setParameter("centerLng", centerLng);
        }

        return executeMarkerQuery(query);
    }

    /**
     * Query 실행 및 MarkerInfo 변환
     */
    private List<FacilityMarkerResponse.MarkerInfo> executeMarkerQuery(Query query) {
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new FacilityMarkerResponse.MarkerInfo(
                        ((Number) row[0]).longValue(),     // id
                        (BigDecimal) row[1],               // latitude
                        (BigDecimal) row[2],               // longitude
                        (String) row[3],                   // facility_type
                        (String) row[4]                    // name
                ))
                .collect(Collectors.toList());
    }
}
