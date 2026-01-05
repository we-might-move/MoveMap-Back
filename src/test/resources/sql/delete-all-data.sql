-- 1. Member 관련 자식 테이블부터 삭제 (외래키 제약조건 고려)

-- 1-1. 점수/기록 관련
DELETE FROM member_score;
DELETE FROM check_in_record;
DELETE FROM self_record;
DELETE FROM steps_record;

-- 1-2. 찜하기/관계 관련
DELETE FROM member_facility;
DELETE FROM member_program;
DELETE FROM parent_child;

-- 1-3. 리뷰 관련
DELETE FROM facility_review;
DELETE FROM program_review;

-- 1-4. 알림 관련
DELETE FROM notification;

-- 1-5. 리그 관련 점수 데이터
DELETE FROM daily_region_score;
DELETE FROM weekly_region_score;
DELETE FROM league_status;

-- 2. Member 삭제 (모든 외래키 참조 제거 후)
DELETE FROM member;

-- 3. Sequence 초기화 (테스트 데이터용)
-- region_type, facility, program의 sequence는 건드리지 않음!
ALTER SEQUENCE member_id_seq RESTART WITH 1;
ALTER SEQUENCE notification_id_seq RESTART WITH 1;
ALTER SEQUENCE parent_child_id_seq RESTART WITH 1;
ALTER SEQUENCE member_facility_id_seq RESTART WITH 1;
ALTER SEQUENCE member_program_id_seq RESTART WITH 1;
ALTER SEQUENCE member_score_id_seq RESTART WITH 1;
ALTER SEQUENCE check_in_record_id_seq RESTART WITH 1;
ALTER SEQUENCE self_record_id_seq RESTART WITH 1;
ALTER SEQUENCE steps_record_id_seq RESTART WITH 1;
ALTER SEQUENCE facility_review_id_seq RESTART WITH 1;
ALTER SEQUENCE program_review_id_seq RESTART WITH 1;
ALTER SEQUENCE daily_region_score_id_seq RESTART WITH 1;
ALTER SEQUENCE weekly_region_score_id_seq RESTART WITH 1;
ALTER SEQUENCE league_status_id_seq RESTART WITH 1;