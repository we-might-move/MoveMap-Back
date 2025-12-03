package org.wemightmove.movemap.domain.program.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.wemightmove.movemap.domain.program.dto.request.ProgramListBySearchRequest;
import org.wemightmove.movemap.domain.program.dto.request.ProgramMarkerRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramListResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wemightmove.movemap.global.util.ProgramBitmaskUtil.ageToBitmask;
import static org.wemightmove.movemap.global.util.ProgramBitmaskUtil.weekdaysToBitmask;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProgramRepositoryCustomImpl implements ProgramRepositoryCustom {

    private static final double DEFAULT_RADIUS_METERS = 5000.0;
    private final EntityManager entityManager;

    @Override
    public List<ProgramMarkerResponse.MarkerInfo> findMakersByRegionCode(
            BigDecimal lat,
            BigDecimal lng,
            int maxResults,
            Long memberId
    ) {
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

    @Override
    public List<ProgramMarkerResponse.MarkerInfo> findMarkersByViewport(
            ProgramMarkerRequest request,
            String regionCode,
            List<FacilityType> facilityTypes,
            List<Integer> weekDayTypes,
            Long memberId
    ) {
        StringBuilder sql = new StringBuilder();

        sql.append("""
            SELECT
                p.latitude,
                p.longitude,
                COUNT(p.id) as program_count,
                MIN(p.name) as representative_name,
                CASE WHEN COUNT(mp.id) > 0 THEN true ELSE false END as has_bookmark
            FROM program p
            LEFT JOIN member_program mp ON p.id = mp.program_id AND mp.member_id = :memberId
            WHERE 1=1
            """);

        // Viewport Bounding Box
        sql.append("""
            AND p.latitude BETWEEN :southWestLat AND :northEastLat
            AND p.longitude BETWEEN :southWestLng AND :northEastLng
            """);

        // ✅ 동적 조건 생성
        List<String> conditions = new ArrayList<>();

        // 지역
        if (regionCode != null) {
            conditions.add("p.region_cd LIKE :regionCode || '%'");
        }

        // 시설 타입
        if (facilityTypes != null && !facilityTypes.isEmpty()) {
            conditions.add("p.facility_type = ANY(:facilityTypes)");
        }

        // ✅ 가격 필터 (0 체크 + NULL 처리)
        boolean isFreeSearch = (request.minPrice() != null && request.minPrice() == 0
                && request.maxPrice() != null && request.maxPrice() == 0);

        if (isFreeSearch) {
            // 무료 프로그램만 검색
            conditions.add("p.price = 0");
        } else {
            // 일반 가격 범위
            if (request.minPrice() != null && request.minPrice() > 0) {
                conditions.add("p.price IS NOT NULL AND p.price >= :minPrice");
            }

            if (request.maxPrice() != null && request.maxPrice() > 0) {
                conditions.add("p.price IS NOT NULL AND p.price <= :maxPrice");
            }
        }

        // ✅ 요일 비트마스크
        if (weekDayTypes != null && !weekDayTypes.isEmpty()) {
            conditions.add("(p.weekday_number & :weekdayBitmask) > 0");
        }

        // ✅ 연령 비트마스크
        if (request.minAge() != null || request.maxAge() != null) {
            conditions.add("(p.target IS NULL OR (p.target & :targetBitmask) > 0)");
        }

        // ✅ WHERE 절에 조건 추가 (여기서 한 번만!)
        if (!conditions.isEmpty()) {
            sql.append(" AND (");
            sql.append(String.join(" AND ", conditions));
            sql.append(")\n");
        }

        // 키워드
        if (request.keyword() != null && !request.keyword().isBlank()) {
            sql.append(" OR ").append("(p.name ILIKE :keyword)");
        }

        // GROUP BY
        sql.append("""
            GROUP BY p.latitude, p.longitude
            """);

        // 정렬
        if (request.hasSearchConditions()) {
            sql.append("ORDER BY program_count DESC\n");
        } else {
            sql.append("""
                ORDER BY MIN(
                    ST_DistanceSphere(
                        ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326),
                        ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326)
                    )
                ) ASC
                """);
        }

        sql.append(" LIMIT :maxResults");

        // ✅ Query 생성 및 파라미터 바인딩
        Query query = entityManager.createNativeQuery(sql.toString());

        // 필수 파라미터
        query.setParameter("northEastLat", request.northEastLat());
        query.setParameter("northEastLng", request.northEastLng());
        query.setParameter("southWestLat", request.southWestLat());
        query.setParameter("southWestLng", request.southWestLng());
        query.setParameter("memberId", memberId);
        query.setParameter("maxResults", request.maxResults());

        // 조건부 파라미터
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

        // ✅ 가격 파라미터 (0 체크)
        if (request.minPrice() != null && request.minPrice() > 0) {
            query.setParameter("minPrice", request.minPrice());
        }

        if (request.maxPrice() != null && request.maxPrice() > 0) {
            query.setParameter("maxPrice", request.maxPrice());
        }

        // ✅ 요일 파라미터
        if (weekDayTypes != null && !weekDayTypes.isEmpty()) {
            int weekdayBitmask = calculateWeekdayBitmask(weekDayTypes);
            query.setParameter("weekdayBitmask", weekdayBitmask);
        }

        // ✅ 연령 파라미터
        if (request.minAge() != null || request.maxAge() != null) {
            int targetBitmask = calculateTargetBitmask(request.minAge(), request.maxAge());
            query.setParameter("targetBitmask", targetBitmask);
            log.debug("Age filter - minAge: {}, maxAge: {}, bitmask: {}",
                    request.minAge(), request.maxAge(), targetBitmask);
        }

        // 거리순 정렬용 중심점
        if (!request.hasSearchConditions()) {
            double centerLat = (request.northEastLat() + request.southWestLat()) / 2;
            double centerLng = (request.northEastLng() + request.southWestLng()) / 2;
            query.setParameter("centerLat", centerLat);
            query.setParameter("centerLng", centerLng);
        }

        log.debug("Executing viewport marker query - SQL: {}", sql.toString());

        return executeProgramMarkerQuery(query);
    }

    @Override
    public List<ProgramListResponse.ProgramItem> findProgramsByRegion(
            Long memberId,
            String regionCode,
            Long cursor,
            int size
    ) {
        StringBuilder sql = new StringBuilder();

        sql.append("""
            SELECT
                p.id,
                p.name,
                p.facility_type,
                p.facility_subtype,
                p.latitude,
                p.longitude,
                p.address,
                p.hmpg_url,
                p.begin_date,
                p.end_date,
                p.weekday_number,
                p.price,
                p.start_time,
                p.end_time,
                p.target,
                p.capacity,
                NULL as distance,
                COALESCE(AVG(pr.rating), 0) as avg_rating,
                COUNT(pr.id) as review_count,
                CASE WHEN mp.id IS NOT NULL THEN true ELSE false END as is_bookmarked
            FROM program p
            LEFT JOIN program_review pr ON p.id = pr.program_id
            LEFT JOIN member_program mp ON p.id = mp.program_id AND mp.member_id = :memberId
            WHERE p.region_cd LIKE :regionPrefix
            """);

        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);
        params.put("regionPrefix", regionCode + "%");

        // 커서 기반 페이징
        if (cursor != null) {
            sql.append("AND p.id < :cursor ");
            params.put("cursor", cursor);
        }

        sql.append("""
            GROUP BY p.id, p.name, p.facility_type, p.facility_subtype,
                     p.latitude, p.longitude, p.address, p.region_cd, p.hmpg_url,
                     p.begin_date, p.end_date, p.weekday_number, p.price,
                     p.start_time, p.end_time, p.target, p.capacity, mp.id
            ORDER BY p.id DESC
            LIMIT :size
            """);

        params.put("size", size);

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(ProgramListResponse.ProgramItem::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgramListResponse.ProgramItem> findProgramsByViewport(Long memberId, ProgramListBySearchRequest request, String regionCode, List<FacilityType> facilityTypes, List<Integer> weekDayTypes, int size) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        params.put("memberId", memberId);

        // SELECT 절
        sql.append("""
            SELECT
                p.id,
                p.name,
                p.facility_type,
                p.facility_subtype,
                p.latitude,
                p.longitude,
                p.address,
                p.hmpg_url,
                p.begin_date,
                p.end_date,
                p.weekday_number,
                p.price,
                p.start_time,
                p.end_time,
                p.target,
                p.capacity,
            """);

        // 거리 계산 (사용자 위치 제공 시)
        if (request.userLat() != null && request.userLng() != null) {
            sql.append("""
                ST_Distance(
                    p.location::geography,
                    ST_SetSRID(ST_MakePoint(:userLng, :userLat), 4326)::geography
                ) / 1000.0 as distance,
                """);
            params.put("userLat", request.userLat());
            params.put("userLng", request.userLng());
        } else {
            sql.append("NULL as distance, ");
        }

        // 평점, 리뷰 수, 북마크 여부
        sql.append("""
                COALESCE(AVG(pr.rating), 0) as avg_rating,
                COUNT(pr.id) as review_count,
                CASE WHEN mp.id IS NOT NULL THEN true ELSE false END as is_bookmarked
            FROM program p
            LEFT JOIN program_review pr ON p.id = pr.program_id
            LEFT JOIN member_program mp ON p.id = mp.program_id AND mp.member_id = :memberId
            WHERE 1=1
            """);

        // 뷰포트 바운딩 박스 필터
        sql.append("""
            AND p.latitude BETWEEN :southWestLat AND :northEastLat
            AND p.longitude BETWEEN :southWestLng AND :northEastLng
            """);
        params.put("northEastLat", request.northEastLat());
        params.put("northEastLng", request.northEastLng());
        params.put("southWestLat", request.southWestLat());
        params.put("southWestLng", request.southWestLng());

        // 지역 필터
        if (regionCode != null) {
            conditions.add("p.region_cd LIKE :regionCode || '%'");
            params.put("regionCode", regionCode);
        }

        // 시설 타입 필터
        if (facilityTypes != null && !facilityTypes.isEmpty()) {
            conditions.add("p.facility_type = ANY(:facilityTypes)");
            String[] typeArray = facilityTypes.stream()
                    .map(Enum::name)
                    .toArray(String[]::new);
            params.put("facilityTypes", typeArray);
        }

        // 가격 필터
        if (request.isFreeSearch()) {
            conditions.add("p.price = 0");
        } else {
            if (request.minPrice() != null && request.minPrice() > 0) {
                conditions.add("p.price IS NOT NULL AND p.price >= :minPrice");
                params.put("minPrice", request.minPrice());
            }
            if (request.maxPrice() != null && request.maxPrice() > 0) {
                conditions.add("p.price IS NOT NULL AND p.price <= :maxPrice");
                params.put("maxPrice", request.maxPrice());
            }
        }

        // 요일 필터
        if (weekDayTypes != null && !weekDayTypes.isEmpty()) {
            conditions.add("(p.weekday_number & :weekdayBitmask) > 0");
            int weekdayBitmask = weekdaysToBitmask(weekDayTypes);
            params.put("weekdayBitmask", weekdayBitmask);
        }

        // 연령 필터
        if (request.minAge() != null || request.maxAge() != null) {
            conditions.add("(p.target IS NULL OR (p.target & :targetBitmask) > 0)");
            int targetBitmask = ageToBitmask(request.minAge(), request.maxAge());
            params.put("targetBitmask", targetBitmask);
        }

        // 날짜 필터
        if (request.startDate() != null) {
            conditions.add("(p.end_date IS NULL OR p.end_date >= :startDate)");
            params.put("startDate", request.startDate());
        }
        if (request.endDate() != null) {
            conditions.add("(p.begin_date IS NULL OR p.begin_date <= :endDate)");
            params.put("endDate", request.endDate());
        }

        // 조건 추가
        if (!conditions.isEmpty()) {
            sql.append(" AND (");
            sql.append(String.join(" AND ", conditions));
            sql.append(")\n");
        }

        // 키워드 검색
        if (request.keyword() != null && !request.keyword().isBlank()) {
            sql.append(" OR ").append("(p.name ILIKE :keyword OR p.address ILIKE :keyword OR p.facility_subtype ILIKE :keyword)");
            params.put("keyword", "%" + request.keyword() + "%");
        }

        // 커서 기반 페이징
        if (request.cursor() != null) {
            sql.append("AND p.id < :cursor ");
            params.put("cursor", request.cursor());
        }

        // GROUP BY
        sql.append("""
            GROUP BY p.id, p.name, p.facility_type, p.facility_subtype,
                     p.latitude, p.longitude, p.address, p.region_cd, p.hmpg_url,
                     p.begin_date, p.end_date, p.weekday_number, p.price,
                     p.start_time, p.end_time, p.target, p.capacity, mp.id
            """);

        // ORDER BY
        if (request.userLat() != null && request.userLng() != null && !request.hasSearchConditions()) {
            sql.append("""
                ORDER BY ST_Distance(
                    p.location::geography,
                    ST_SetSRID(ST_MakePoint(:userLng, :userLat), 4326)::geography
                ) ASC, p.id DESC
                """);
        } else {
            sql.append("ORDER BY p.id DESC ");
        }

        sql.append("LIMIT :size");
        params.put("size", size);

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(ProgramListResponse.ProgramItem::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgramSimpleListResponse.ProgramSimpleItem> searchProgramsByKeyword(
            String keyword,
            Long cursor,
            int size
    ) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        sql.append("""
        SELECT 
            p.id,
            p.name as program_name,
            p.facility_name as facility_name,
            p.facility_subtype,
            p.address
        FROM program p
        WHERE 1=1
        """);

        // 커서 기반 페이징
        if (cursor != null) {
            sql.append("AND p.id > :cursor ");
            params.put("cursor", cursor);
        }

        // ✅ Prefix 검색 (B-Tree 인덱스 활용)
        sql.append("""
            AND (
                p.name_normalized ILIKE :keyword
                OR p.facility_name_normalized ILIKE :keyword
            )
            """);
        params.put("keyword", keyword + "%");

        // 정렬 및 제한
        sql.append("""
        ORDER BY p.id ASC
        LIMIT :size
        """);
        params.put("size", size);

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(ProgramSimpleListResponse.ProgramSimpleItem::mapToDTO)
                .collect(Collectors.toList());
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

    private int calculateWeekdayBitmask(List<Integer> weekdays) {
        return weekdaysToBitmask(weekdays);
    }

    private int calculateTargetBitmask(Integer minAge, Integer maxAge) {
        return ageToBitmask(minAge, maxAge);
    }
}