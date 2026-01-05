-- facility_name_normalized 컬럼 추가 (공백 제거)
ALTER TABLE program
    ADD COLUMN IF NOT EXISTS facility_name_normalized VARCHAR(200)
        GENERATED ALWAYS AS (REPLACE(COALESCE(facility_name, ''), ' ', '')) STORED;

-- B-Tree 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_program_facility_name_normalized_btree
    ON program(facility_name_normalized);