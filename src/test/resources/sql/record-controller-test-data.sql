-- ============================================
-- Member
-- ============================================
--학생1
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at) VALUES
    (1, 100001, 'student1@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'STUDENT', '서울중학교', '테스트학생1', '11680', 'student-uuid-0001', 165.0, 55.0, false, 15, 'MAN', NOW(), NOW());
--부모1
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at) VALUES
    (2, 100002, 'parent1@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'PARENT', NULL, '테스트부모1', '11680', 'parent-uuid-0001', 170.5, 65.0, false, 40, 'MAN', NOW(), NOW());
--학생2
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at) VALUES
    (3, 100003, 'student2@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'STUDENT', '서울고등학교', '테스트학생2', '11680', 'student-uuid-0002', 160.0, 50.0, false, 17, 'WOMAN', NOW(), NOW());
--부모2
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at) VALUES
    (4, 100004, 'parent2@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'PARENT', NULL, '테스트부모2', '11680', 'parent-uuid-0002', 175.0, 70.0, false, 42, 'WOMAN', NOW(), NOW());

-- ============================================
-- ParentChild
-- ============================================
INSERT INTO parent_child (id, parent_id, child_id) VALUES
    (1, 2, 1);  -- 부모1 <-> 자녀1 관계
INSERT INTO parent_child (id, parent_id, child_id) VALUES
    (2, 4, 3);  -- 부모2 <-> 자녀2 관계

-- ============================================
-- SelfRecord
-- ============================================
-- 같은 날짜에 여러 개의 셀프 기록
insert into self_record (
    date,
    exercise_type,
    member_id,
    duration_minutes
) values
      ('2026-01-06', 'FITNESS', 1, 60),
      ('2026-01-06', 'LEISURE', 1, 30),
      ('2026-01-05', 'DANCE', 3, 70),
      ('2026-01-05', 'AQUATIC', 3, 100),
      ('2026-01-05', 'COMPLEX', 3, 120);

-- 다른 날짜 데이터
insert into self_record (
    date,
    exercise_type,
    member_id,
    duration_minutes
) values
    ('2026-01-05', 'DANCE', 1, 45),
    ('2026-01-06', 'ETC', 3, 15);

-- ============================================
-- StepsRecord
-- ============================================
insert into steps_record (
    id,
    date,
    count,
    distance,
    member_id,
    last_synced_at
) values
      (1, '2026-01-06', 3000, 1.52, 1, '2026-01-06 14:20:10'),
      (2, '2026-01-05', 10000, 5.01, 1, '2026-01-05 21:45:23'),
      (3, '2026-01-05', 20000, 10.56, 3, '2026-01-05 20:12:12');


-- ============================================
-- CheckInRecord
-- ============================================
INSERT INTO check_in_record (
    id,
    member_id,
    facility_id,
    date,
    check_in_at,
    check_out_at,
    duration_minutes
) VALUES
      (1,1,2,'2026-01-05','2026-01-05 09:30:00','2026-01-05 11:20:00', 110),
      (2,1,5,'2026-01-05','2026-01-05 11:30:00','2026-01-05 12:00:00', 30),
      (3,1,6,'2026-01-05','2026-01-05 12:30:00','2026-01-05 13:00:00', 30),
      (4,1,7,'2026-01-05','2026-01-05 13:30:00','2026-01-05 14:00:00', 30),
      (5,1,8,'2026-01-05','2026-01-05 14:30:00','2026-01-05 15:00:00', 30),
      (6,1,3,'2026-01-06','2026-01-06 09:30:00',NULL, 0);

INSERT INTO check_in_record (
    id,
    member_id,
    facility_id,
    date,
    check_in_at,
    check_out_at,
    duration_minutes
) VALUES
      (7,3,4,'2026-01-06','2026-01-06 09:30:00','2026-01-06 10:30:00', 60);


-- ============================================
-- MemberScore
-- ============================================

INSERT INTO member_score (
    created_at,
    updated_at,
    date,
    total_self_duration,
    total_checkin_duration,
    total_steps,
    member_id
) VALUES
-- CASE A: self > 0 => true (경계: 1분이라도 >0이면 true)
(now(), now(), '2026-01-03', 1, 0, 0, 1),
-- CASE B: checkin > 0 => true (경계: 1분이라도 >0이면 true)
(now(), now(), '2026-01-05', 0, 1, 0, 1),
-- CASE C: steps == 9999 => false (경계 바로 아래)
(now(), now(), '2026-01-10', 0, 0, 9999, 1),
-- CASE D: steps == 10000 => true (경계값)
(now(), now(), '2026-01-11', 0, 0, 10000, 1),
-- CASE E: steps > 10000 => true (충분히 큰 값)
(now(), now(), '2026-01-12', 0, 0, 60000, 1),
-- CASE F: self=0, checkin=0, steps=0 => false (아무 기록도 없는 날을 "DB에 row로 넣는" 케이스)
(now(), now(), '2026-01-15', 0, 0, 0, 1),
-- CASE G: self>0 AND checkin>0 AND steps<10000 => true (복합 조건)
(now(), now(), '2026-01-20', 30, 30, 3000, 1);

-- ============================================
-- 9. Sequence 재설정 (ID 충돌 방지)
-- ============================================
SELECT setval('region_type_id_seq', (SELECT MAX(id) FROM region_type));
SELECT setval('member_id_seq', (SELECT MAX(id) FROM member));
SELECT setval('check_in_record_id_seq', (SELECT MAX(id) FROM check_in_record));
SELECT setval('steps_record_id_seq', (SELECT MAX(id) FROM steps_record));
SELECT setval('self_record_id_seq', (SELECT MAX(id) FROM self_record));
SELECT setval('facility_id_seq', (SELECT MAX(id) FROM facility));
SELECT setval('member_score_id_seq', (SELECT MAX(id) FROM member_score));
SELECT setval('parent_child_id_seq', (SELECT MAX(id) FROM parent_child));
