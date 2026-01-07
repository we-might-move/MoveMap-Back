-- ============================================
-- Member 테스트 데이터
-- ============================================
-- 설계 원칙:
-- 1. 각 테스트 시나리오에 필요한 최소한의 데이터만 생성
-- 2. ID를 명시적으로 지정하여 테스트에서 예측 가능하게 사용
-- 3. 외래키 관계를 고려한 순서대로 INSERT

-- ============================================
-- 1. Member 기본 데이터 (부모 2명, 자녀 2명)
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

-- 삭제된 회원 (탈퇴 테스트용)
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at)
VALUES (5, 100005, 'deleted@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'STUDENT', NULL, '삭제된회원', '11680', 'deleted-uuid-0001', 170.0, 60.0, true, 20, 'MAN', NOW(), NOW());

-- ============================================
-- 2. ParentChild 관계 (이미 연결된 부모-자녀)
-- ============================================
-- 부모2 ↔ 자녀2는 이미 연결됨 (중복 연결 테스트용)
INSERT INTO parent_child (id, parent_id, child_id)
VALUES (1, 2, 4);

-- ============================================
-- 3. MemberFacility (북마크 데이터)
-- ============================================
-- 부모1이 시설 1, 2, 3을 북마크
INSERT INTO member_facility (id, member_id, facility_id)
VALUES
    (1, 1, 1),
    (2, 1, 2),
    (3, 1, 3);

-- ============================================
-- 4. MemberProgram (프로그램 북마크)
-- ============================================
-- 부모1이 프로그램 1, 2를 북마크
INSERT INTO member_program (id, member_id, program_id)
VALUES
    (1, 1, 1),
    (2, 1, 2);

-- ============================================
-- 6. 시퀀스 업데이트
-- ============================================
SELECT setval('member_id_seq', 100);
SELECT setval('parent_child_id_seq', 100);
SELECT setval('member_facility_id_seq', 100);
SELECT setval('member_program_id_seq', 100);