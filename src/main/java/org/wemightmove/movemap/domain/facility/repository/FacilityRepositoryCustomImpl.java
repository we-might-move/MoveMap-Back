package org.wemightmove.movemap.domain.facility.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityInitialListRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityMarkerRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilitySearchListRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityListResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FacilityRepositoryCustomImpl implements FacilityRepositoryCustom {

    private static final double DEFAULT_RADIUS_METERS = 1000.0;
    private static final int FETCH_SIZE_FOR_HAS_NEXT = 1; // hasNext 판단용 추가 조회 개수

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
        List<String> orConditions = new ArrayList<>();
        List<String> filterConditions = new ArrayList<>();

        if (regionCode != null) {
            filterConditions.add("f.region_cd LIKE :regionCode || '%'");
        }

        if (facilityTypes != null && !facilityTypes.isEmpty()) {
            filterConditions.add("f.facility_type = ANY(:facilityTypes)");
        }

        if (request.isVoucherAvailable() != null && request.isVoucherAvailable()) {
            filterConditions.add("f.is_voucher_available = true");
        }

        // WHERE 절에 조건 추가
        if (!filterConditions.isEmpty()) {
            String filterGroup = "(" + String.join(" AND ", filterConditions) + ")";
            orConditions.add(filterGroup);
        }

        if (request.keyword() != null && !request.keyword().isBlank()) {
            orConditions.add("(f.name ILIKE :keyword)");
        }

        if (!orConditions.isEmpty()) {
            sql.append(" AND (");
            sql.append(String.join(" OR ", orConditions));
            sql.append(")\n");
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

        log.info("query : \n{}", sql);

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
            log.info("regionCode = {}", regionCode);
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

            log.info("centerLat = {}", centerLat);
            log.info("centerLng = {}", centerLng);
        }

        return executeMarkerQuery(query);
    }

    @Override
    public List<FacilityListResponse.FacilityInfo> findListByRegionCode(FacilityInitialListRequest request, BigDecimal lat, BigDecimal lng, String regionCode, Long memberId) {

        String sql = """
            SELECT 
                f.id,
                f.name,
                f.latitude,
                f.longitude,
                f.facility_type,
                f.facility_subtype,
                f.address,
                f.is_voucher_available,
                ST_Distance(
                    f.location::geography,
                    ST_SetSRID(ST_MakePoint(:userLng, :userLat), 4326)::geography
                ) AS distance_meters,
                COALESCE(AVG(fr.rating), 0) AS avg_rating,
                COUNT(fr.id) AS review_count,
                CASE WHEN mf.id IS NOT NULL THEN true ELSE false END AS is_bookmarked
            FROM facility f
            LEFT JOIN facility_review fr ON f.id = fr.facility_id
            LEFT JOIN member_facility mf ON f.id = mf.facility_id AND mf.member_id = :memberId
            WHERE ST_DWithin(
                f.location::geography,
                ST_SetSRID(ST_MakePoint(:userLng, :userLat), 4326)::geography,
                :radius
            )
            AND f.region_cd LIKE :regionCode || '%'
            """;

            // 커서 기반 페이징
            if (request.hasCursor()) {
                sql += " AND f.id < :cursor";
            }

            sql += """
            GROUP BY f.id, f.name, f.latitude, f.longitude, f.facility_type, 
                     f.facility_subtype, f.address, f.is_voucher_available, 
                     distance_meters, mf.id
            ORDER BY distance_meters ASC, f.id DESC
            LIMIT :limit
            """;

            log.info("findListByRegionCode = \n{}", sql);

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("userLat", lat);
            query.setParameter("userLng", lng);
            query.setParameter("radius", DEFAULT_RADIUS_METERS);
            query.setParameter("regionCode", regionCode);
            query.setParameter("memberId", memberId);

            if (request.hasCursor()) {
                query.setParameter("cursor", request.cursor());
            }

            // hasNext 판단을 위해 size + 1개 조회
            query.setParameter("limit", request.size() + FETCH_SIZE_FOR_HAS_NEXT);

            return executeFacilityListQuery(query);
    }

    @Override
    public List<FacilityListResponse.FacilityInfo> findListByViewport(FacilitySearchListRequest request, String regionCode, List<FacilityType> facilityTypes, Long memberId) {
        StringBuilder sql = new StringBuilder();

        sql.append("""
            SELECT 
                f.id,
                f.name,
                f.latitude,
                f.longitude,
                f.facility_type,
                f.facility_subtype,
                f.address,
                f.is_voucher_available,
            """);

        // 검색 조건 없을 때만 거리 계산 (성능 최적화)
        if (!request.hasSearchConditions()) {
            sql.append("""
                ST_Distance(
                    f.location::geography,
                    ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326)::geography
                ) AS distance_meters,
                """);
        } else {
            sql.append("NULL AS distance_meters,\n");
        }

        sql.append("""
                COALESCE(AVG(fr.rating), 0) AS avg_rating,
                COUNT(fr.id) AS review_count,
                CASE WHEN mf.id IS NOT NULL THEN true ELSE false END AS is_bookmarked
            FROM facility f
            LEFT JOIN facility_review fr ON f.id = fr.facility_id
            LEFT JOIN member_facility mf ON f.id = mf.facility_id AND mf.member_id = :memberId
            WHERE f.latitude BETWEEN :southWestLat AND :northEastLat
            AND f.longitude BETWEEN :southWestLng AND :northEastLng
            """);

        // ✅ 동적 검색 조건 추가
        List<String> orConditions = new ArrayList<>();
        List<String> filterConditions = new ArrayList<>();

        if (regionCode != null) {
            filterConditions.add("f.region_cd LIKE :regionCode || '%'");
        }

        if (facilityTypes != null && !facilityTypes.isEmpty()) {
            filterConditions.add("f.facility_type = ANY(:facilityTypes)");
        }

        if (request.isVoucherAvailable() != null && request.isVoucherAvailable()) {
            filterConditions.add("f.is_voucher_available = true");
        }

        // ✅ 커서 조건 추가
        if (request.hasCursor()) {
            filterConditions.add("f.id < :cursor");
        }

        // ✅ 조건들을 AND로 연결 (공백 명확히 관리)
        if (!filterConditions.isEmpty()) {
            String filterGroup = "(" + String.join(" AND ", filterConditions) + ")";
            orConditions.add(filterGroup);
        }

        if (request.keyword() != null && !request.keyword().isBlank()) {
            orConditions.add("(f.name ILIKE :keyword)");
        }

        if (!orConditions.isEmpty()) {
            sql.append(" AND (");
            sql.append(String.join(" OR ", orConditions));
            sql.append(")\n");
        }

        // ✅ GROUP BY (앞에 공백 확보)
        sql.append("""
            GROUP BY f.id, f.name, f.latitude, f.longitude, f.facility_type, 
                     f.facility_subtype, f.address, f.is_voucher_available
            """);

        // 거리 계산이 있는 경우 GROUP BY에 추가
        if (!request.hasSearchConditions()) {
            sql.append(", distance_meters\n");
        }

        sql.append(", mf.id\n");

        // ✅ 정렬: 검색 조건 있으면 최신순, 없으면 거리순
        if (request.hasSearchConditions()) {
            sql.append("ORDER BY f.id DESC\n");
        } else {
            sql.append("ORDER BY distance_meters ASC, f.id DESC\n");
        }

        sql.append("LIMIT :limit");

        log.info("findListByViewport = \n{}", sql);

        // ✅ Query 생성 및 파라미터 바인딩
        Query query = entityManager.createNativeQuery(sql.toString());

        // Viewport 파라미터
        query.setParameter("northEastLat", request.northEastLat());
        query.setParameter("northEastLng", request.northEastLng());
        query.setParameter("southWestLat", request.southWestLat());
        query.setParameter("southWestLng", request.southWestLng());
        query.setParameter("memberId", memberId);

        // 거리 계산용 중심점 (검색 조건 없을 때만)
        if (!request.hasSearchConditions()) {
            query.setParameter("centerLat", (request.northEastLat() + request.southWestLat()) / 2.0);
            query.setParameter("centerLng", (request.northEastLng() + request.southWestLng()) / 2.0);

            log.info("centerLat : {}", (request.northEastLat() + request.southWestLat()) / 2.0);
            log.info("centerLng : {}", (request.northEastLng() + request.southWestLng()) / 2.0);
        }

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

        // ✅ 커서 파라미터 (조건이 있을 때만 바인딩)
        if (request.hasCursor()) {
            query.setParameter("cursor", request.cursor());
        }

        query.setParameter("limit", request.size() + FETCH_SIZE_FOR_HAS_NEXT);

        return executeFacilityListQuery(query);
    }

    /**
     * Query 실행 및 FacilityInfo 변환
     */
    private List<FacilityListResponse.FacilityInfo> executeFacilityListQuery(Query query) {
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new FacilityListResponse.FacilityInfo(
                        ((Number) row[0]).longValue(),                    // id
                        (String) row[1],                                  // name
                        (BigDecimal) row[2],                              // latitude
                        (BigDecimal) row[3],                              // longitude
                        (String) row[4],                                  // facility_type
                        (String) row[5],                                  // facility_subtype
                        (String) row[6],                                  // address
                        (Boolean) row[7],                                 // is_voucher_available
                        row[8] != null ? ((Number) row[8]).doubleValue() : null,  // distance_meters
                        row[9] != null ? ((Number) row[9]).doubleValue() : 0.0,   // avg_rating
                        row[10] != null ? ((Number) row[10]).longValue() : 0L,    // review_count
                        (Boolean) row[11]                                 // is_bookmarked
                ))
                .collect(Collectors.toList());
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
