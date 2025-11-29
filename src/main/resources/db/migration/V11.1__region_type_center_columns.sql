-- RegionType 에 중심 좌표 컬럼 추가

-- 1. 컬럼 추가
ALTER TABLE region_type
    ADD COLUMN center_latitude NUMERIC(10, 8),
    ADD COLUMN center_longitude NUMERIC(11, 8),
    ADD COLUMN center_location GEOMETRY(Point, 4326);

-- 2. 인덱스 생성 (공간 쿼리 최적화)
CREATE INDEX idx_region_type_location
    ON region_type
        USING GIST(center_location);

-- 3. 컬럼 설명 추가
COMMENT ON COLUMN region_type.center_latitude IS '지역 중심점 위도 (시설 데이터 기반 계산)';
COMMENT ON COLUMN region_type.center_longitude IS '지역 중심점 경도 (시설 데이터 기반 계산)';
COMMENT ON COLUMN region_type.center_location IS '지역 중심점 PostGIS Point (SRID 4326)';