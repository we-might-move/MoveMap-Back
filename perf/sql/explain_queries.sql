-- ================================================================
-- 조건별로 실행해 실행계획(Seq Scan vs Index Scan)과 시간을 캡처
-- ================================================================

-- ① program /search 의 실제 쿼리 (prefix, name_normalized)
EXPLAIN (ANALYZE, BUFFERS)
SELECT p.id, p.name, p.facility_name, p.facility_subtype, p.address
FROM program p
WHERE p.name_normalized ILIKE '수영%' OR p.facility_name_normalized ILIKE '수영%'
ORDER BY p.id
LIMIT 20;

-- ② facility /search 의 실제 쿼리 (양쪽 와일드카드)
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM facility f
WHERE f.name LIKE '%수영%' OR f.facility_subtype LIKE '%수영%'
LIMIT 30;

-- ③ 짧은 검색어(1글자) — pg_trgm 효율 급락 케이스 확인용
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM facility f
WHERE f.name LIKE '%강%'
LIMIT 30;

-- ================================================================
-- pg_stat_statements: 워크로드(k6) 실행 전후로 사용
-- ================================================================

-- (측정 직전) 통계 초기화
-- SELECT pg_stat_statements_reset();

-- (측정 직후) 검색 쿼리의 서버측 순수 실행시간 집계
SELECT calls,
       round(mean_exec_time::numeric, 2)  AS mean_ms,
       round(total_exec_time::numeric, 2) AS total_ms,
       rows
FROM pg_stat_statements
WHERE query ILIKE '%facility%' OR query ILIKE '%program%'
ORDER BY total_exec_time DESC
LIMIT 20;
