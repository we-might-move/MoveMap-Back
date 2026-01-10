-- ============================================
-- Member
-- ============================================
--학생1
INSERT INTO member (id, kakao_id, email, password, role, school, nickname, region_cd, uuid, height, weight, is_deleted, age, sex, created_at, updated_at) VALUES
    (1, 100001, 'student1@test.com', '$2a$10$hhhp9v1xqyMt6cK50M.5oeYpcQeV7x8LvguaIXBSKvbzMixRjRNum', 'STUDENT', '서울중학교', '테스트학생1', '11999', 'student-uuid-0001', 165.0, 55.0, false, 15, 'MAN', NOW(), NOW());

-- ============================================
-- RegionType (테스트용)
-- ============================================

INSERT INTO region_type (parent_id, prefix, name) VALUES
    ((SELECT id FROM region_type WHERE prefix = '11'), '11999', 'test');

-- ============================================
-- LeagueStatus
-- ============================================
insert into league_status (id, region_id, type)
values (9999, (SELECT id FROM region_type WHERE prefix = '11999'), 'START');