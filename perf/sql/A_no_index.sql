-- 조건 A: 텍스트 인덱스 없음 (현재 상태로 복귀)
-- B에서 만든 trigram 인덱스를 모두 제거해 A(Seq Scan) 상태로 되돌린다.
DROP INDEX IF EXISTS idx_facility_name_trgm;
DROP INDEX IF EXISTS idx_facility_subtype_trgm;
DROP INDEX IF EXISTS idx_program_name_norm_trgm;
DROP INDEX IF EXISTS idx_program_facility_name_norm_trgm;
ANALYZE facility;
ANALYZE program;
