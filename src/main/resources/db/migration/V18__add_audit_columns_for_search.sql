-- ES 마이그레이션 리컨실리에이션(변경분 감지) 및 향후 확장성을 위한 감사 컬럼 추가
-- facility/program에 created_at, updated_at을 두고 UPDATE 시 updated_at을 자동 갱신하는
-- 트리거를 붙인다. 리컨실 잡은 updated_at > lastRun 델타로 변경 행을 찾는다.
-- 주의: 이 컬럼들은 JPA 엔티티(Facility/Program)에는 매핑하지 않는다(DB 전용, 범위 외).
ALTER TABLE facility
    ADD COLUMN created_at timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();
ALTER TABLE program
    ADD COLUMN created_at timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();

CREATE OR REPLACE FUNCTION touch_updated_at() RETURNS trigger AS $$
BEGIN NEW.updated_at = now(); RETURN NEW; END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_facility_touch BEFORE UPDATE ON facility
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_program_touch BEFORE UPDATE ON program
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

CREATE INDEX idx_facility_updated_at ON facility(updated_at);
CREATE INDEX idx_program_updated_at ON program(updated_at);
