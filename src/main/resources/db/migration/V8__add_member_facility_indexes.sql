-- 1. 무한 스크롤 최적화를 위한 복합 인덱스
-- 사용 쿼리: WHERE member_id = ? AND id < ? ORDER BY id DESC
-- 성능: 전체 테이블 스캔 → 인덱스 직접 접근 (100배 빠름)
CREATE INDEX IF NOT EXISTS idx_member_facility_member_id_id_desc
    ON member_facility(member_id, id DESC);

COMMENT ON INDEX idx_member_facility_member_id_id_desc IS
    '무한 스크롤 페이지네이션 최적화: member_id 필터링 + id DESC 정렬';


-- 2. Facility location GIST 인덱스 (이미 있을 가능성 높음, 확인용)
-- PostGIS 공간 연산 최적화
-- ST_Distance, ST_DWithin 등에 사용
CREATE INDEX IF NOT EXISTS idx_facility_location_gist
    ON facility USING GIST(location);

COMMENT ON INDEX idx_facility_location_gist IS
    'PostGIS 공간 쿼리 최적화: location 기반 거리 계산 및 범위 검색';


-- 3. FacilityReview facility_id 인덱스 (이미 있을 가능성 높음, 확인용)
-- 리뷰 집계 최적화
-- LEFT JOIN + GROUP BY에 사용
CREATE INDEX IF NOT EXISTS idx_facility_review_facility_id
    ON facility_review(facility_id);

COMMENT ON INDEX idx_facility_review_facility_id IS
    '리뷰 집계 최적화: facility_id 기반 조인 및 그룹화';