ALTER TABLE member_score
    ADD COLUMN total_self_duration int NOT NULL DEFAULT 0;

ALTER TABLE member_score
    ADD COLUMN total_steps int NOT NULL DEFAULT 0;

ALTER TABLE member_score
    ADD COLUMN total_checkin_duration int NOT NULL DEFAULT 0;

/* 기존 score 칼럼이 있으면 드롭하고 다시 생성할 필요 있음 */

ALTER TABLE member_score
    DROP COLUMN IF EXISTS total_score;
ALTER TABLE member_score
    DROP COLUMN IF EXISTS self_score;
ALTER TABLE member_score
    DROP COLUMN IF EXISTS steps_score;
ALTER TABLE member_score
    DROP COLUMN IF EXISTS checkin_score;
ALTER TABLE member_score
    DROP COLUMN IF EXISTS total_score;

/* GENERATED ALWAYS AS STORED */
ALTER TABLE member_score
    ADD COLUMN self_score int
        GENERATED ALWAYS AS (LEAST((total_self_duration / 30) * 15, 30)) STORED;

ALTER TABLE member_score
    ADD COLUMN steps_score int
        GENERATED ALWAYS AS (LEAST((total_steps / 3000) * 10, 40)) STORED;

ALTER TABLE member_score
    ADD COLUMN checkin_score int
        GENERATED ALWAYS AS (LEAST((total_checkin_duration / 30) * 15, 30)) STORED;

