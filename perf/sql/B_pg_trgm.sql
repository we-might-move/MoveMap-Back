-- 조건 B: pg_trgm GIN 인덱스로 검색 컬럼 가속
-- 각 검색 엔드포인트가 "실제로 필터하는 컬럼"에 인덱스를 건다.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- /facilities/search : f.name / f.facility_subtype 에 %kw% (양쪽 와일드카드)
CREATE INDEX IF NOT EXISTS idx_facility_name_trgm
  ON facility USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_facility_subtype_trgm
  ON facility USING gin (facility_subtype gin_trgm_ops);

-- /programs/search : p.name_normalized / p.facility_name_normalized 에 kw% (prefix, ILIKE)
--   trigram GIN 은 ILIKE 의 prefix/부분일치 모두 가속
CREATE INDEX IF NOT EXISTS idx_program_name_norm_trgm
  ON program USING gin (name_normalized gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_program_facility_name_norm_trgm
  ON program USING gin (facility_name_normalized gin_trgm_ops);

ANALYZE facility;
ANALYZE program;

-- 참고: 1~2글자 검색어는 trigram이 적어 개선이 작거나 오히려 느릴 수 있음.
-- keywords.json 에 "강","수" 같은 짧은 검색어를 포함해 그 차이를 관찰할 것.
