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

-- member_id=1: 기기 2개 (푸시 활성화)
INSERT INTO notification (id, member_id, fcm_token, device_type, device_id, is_push_enabled, created_at, updated_at)
VALUES
    (1, 1, 'ExponentPushToken[test-token-1]', 'ANDROID', 'device-001', true, NOW(), NOW()),
    (2, 1, 'ExponentPushToken[test-token-2]', 'IOS', 'device-002', true, NOW(), NOW());

-- member_id=2: 기기 1개 (푸시 비활성화)
INSERT INTO notification (id, member_id, fcm_token, device_type, device_id, is_push_enabled, created_at, updated_at)
VALUES
    (3, 2, 'ExponentPushToken[test-token-3]', 'ANDROID', 'device-003', false, NOW(), NOW());

-- 시퀀스 업데이트
SELECT setval('member_id_seq', 100);
SELECT setval('notification_id_seq', 100);