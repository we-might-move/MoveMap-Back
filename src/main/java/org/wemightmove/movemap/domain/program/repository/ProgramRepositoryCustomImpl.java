package org.wemightmove.movemap.domain.program.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.wemightmove.movemap.domain.program.dto.request.ProgramMarkerRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

        // 키워드
        if (request.keyword() != null && !request.keyword().isBlank()) {
            conditions.add("p.name ILIKE :keyword");
        }

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
            sql.append(" AND ");
            sql.append(String.join(" AND ", conditions));
            sql.append("\n");
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