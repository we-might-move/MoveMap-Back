-- ============================================
-- 2. Member 데이터 (부모 2명, 자녀 2명)
-- ============================================
-- 부모1: JWT 토큰 발급용 (초대 보내는 역할)
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at) VALUES
    (1, 100001, 'parent1@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'PARENT', NULL, '테스트부모1', '11680', 'parent-uuid-0001', 170.5, 65.0, false, 40, 'MAN', NOW(), NOW());

-- 부모2: 다른 초대 시나리오용
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at) VALUES
    (2, 100002, 'parent2@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'PARENT', NULL, '테스트부모2', '11680', 'parent-uuid-0002', 175.0, 70.0, false, 42, 'WOMAN', NOW(), NOW());

-- 자녀1: JWT 토큰 발급용 (초대 받는 역할)
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at) VALUES
    (3, 100003, 'student1@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'STUDENT', '서울중학교', '테스트학생1', '11680', 'student-uuid-0001', 165.0, 55.0, false, 15, 'MAN', NOW(), NOW());

-- 자녀2: 추가 시나리오용
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at) VALUES
    (4, 100004, 'student2@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'STUDENT', '서울고등학교', '테스트학생2', '11680', 'student-uuid-0002', 160.0, 50.0, false, 17, 'WOMAN', NOW(), NOW());

-- ============================================
-- 3. Notification 데이터 (FCM 토큰)
-- ============================================
-- 부모1의 디바이스
INSERT INTO notification (id, member_id, fcm_token, device_type, device_id, is_push_enabled, created_at, updated_at) VALUES
    (1, 1, 'dummy-fcm-token-parent1-android', 'ANDROID', 'device-parent1-001', true, NOW(), NOW());

-- 자녀1의 디바이스
INSERT INTO notification (id, member_id, fcm_token, device_type, device_id, is_push_enabled, created_at, updated_at) VALUES
    (2, 3, 'dummy-fcm-token-student1-ios', 'IOS', 'device-student1-001', true, NOW(), NOW());

-- 자녀2의 디바이스 (푸시 비활성화 상태)
INSERT INTO notification (id, member_id, fcm_token, device_type, device_id, is_push_enabled, created_at, updated_at) VALUES
    (3, 4, 'dummy-fcm-token-student2-android', 'ANDROID', 'device-student2-001', false, NOW(), NOW());

-- ============================================
-- 6. MemberFacility (찜한 시설) - 자녀1이 시설 찜
-- ============================================
INSERT INTO member_facility (id, member_id, facility_id) VALUES
                                                             (1, 3, 1),  -- 자녀1이 강남휘트니스센터 찜
                                                             (2, 3, 2);  -- 자녀1이 종로축구클럽 찜

-- ============================================
-- 7. MemberProgram (찜한 프로그램) - 자녀1이 프로그램 찜
-- ============================================
INSERT INTO member_program (id, member_id, program_id) VALUES
                                                           (1, 3, 1),  -- 자녀1이 청소년 체력단련 프로그램 찜
                                                           (2, 3, 2);  -- 자녀1이 주말 축구 교실 찜

-- ============================================
-- 8. ParentChild (기존 관계) - 부모2와 자녀2는 이미 관계 맺음
-- ============================================
INSERT INTO parent_child (id, parent_id, child_id) VALUES
    (1, 2, 4);  -- 부모2 <-> 자녀2 관계

INSERT INTO facility_review (id, content, rating, facility_id, member_id, title) VALUES
    (1, '골프 연습장 좋네요.', 5, 1, 1, '전라남도 골프연습장 리뷰');
INSERT INTO facility_review (id, content, rating, facility_id, member_id, title) VALUES
    (2, '골프 연습장 좋네요.', 5, 10, 1, '강남 골프연습장 리뷰');

-- ============================================
-- 9. Sequence 재설정 (ID 충돌 방지)
-- ============================================
SELECT setval('region_type_id_seq', (SELECT MAX(id) FROM region_type));
SELECT setval('member_id_seq', (SELECT MAX(id) FROM member));
SELECT setval('notification_id_seq', (SELECT MAX(id) FROM notification));
SELECT setval('facility_id_seq', (SELECT MAX(id) FROM facility));
SELECT setval('program_id_seq', (SELECT MAX(id) FROM program));
SELECT setval('member_facility_id_seq', (SELECT MAX(id) FROM member_facility));
SELECT setval('member_program_id_seq', (SELECT MAX(id) FROM member_program));
SELECT setval('parent_child_id_seq', (SELECT MAX(id) FROM parent_child));
SELECT setval('facility_review_id_seq', (SELECT MAX(id) FROM facility_review));