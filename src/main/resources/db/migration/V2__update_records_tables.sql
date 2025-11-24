-- ==============================================
-- 1. StepsRecord: last_synced_at 컬럼 추가
-- ==============================================
ALTER TABLE steps_record
    ADD COLUMN last_synced_at TIMESTAMP NULL;


-- ==============================================
-- 2. SelfRecord: duration 컬럼 삭제
-- ==============================================
ALTER TABLE self_record
    DROP COLUMN duration;


-- ==============================================
-- 3. SelfRecord: duration_minutes 컬럼 추가
-- ==============================================
ALTER TABLE self_record
    ADD COLUMN duration_minutes INT NOT NULL DEFAULT 0;

-- 기존 데이터에 대해 NULL 방지를 위해 DEFAULT 0 지정 후 제거해도 됨
ALTER TABLE self_record
    ALTER COLUMN duration_minutes DROP DEFAULT;


-- ==============================================
-- 4. CheckInRecord: type, time 필드 삭제
-- ==============================================
ALTER TABLE check_in_record
    DROP COLUMN type,
    DROP COLUMN time;


-- ==============================================
-- 5. CheckInRecord: check_in_at, check_out_at, duration_minutes 추가
-- ==============================================

ALTER TABLE check_in_record
    ADD COLUMN check_in_at TIMESTAMP NULL,
    ADD COLUMN check_out_at TIMESTAMP NULL,
    ADD COLUMN duration_minutes INT NOT NULL DEFAULT 0;

ALTER TABLE check_in_record
    ALTER COLUMN duration_minutes DROP DEFAULT;