-- 기존 member_id 단일 인덱스 삭제 (복합 인덱스로 대체)
DROP INDEX IF EXISTS idx_member_program_member_id;

-- 커서 페이지네이션 최적화를 위한 복합 인덱스
-- member_id로 필터링 후 mp.id로 정렬하므로 (member_id, id) 순서가 최적
CREATE INDEX idx_member_program_member_id_id
    ON member_program(member_id, id DESC);  -- DESC 정렬에 맞춰 인덱스도 DESC

-- 또는 양방향 정렬을 모두 지원하려면
CREATE INDEX idx_member_program_member_id_id_asc
    ON member_program(member_id, id ASC);

CREATE INDEX idx_member_program_member_id_id_desc
    ON member_program(member_id, id DESC);

-- program 테이블 공간 인덱스 (기존과 동일)
CREATE INDEX IF NOT EXISTS idx_program_location
    ON program USING GIST(location);

-- program_review 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_program_review_program_id_rating
    ON program_review(program_id, rating);