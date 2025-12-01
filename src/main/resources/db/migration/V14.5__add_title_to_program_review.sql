ALTER TABLE program_review
    ADD COLUMN title VARCHAR(100);

-- 기존 데이터에 대한 기본값 설정 (필요시)
UPDATE program_review
SET title = '제목 없음'
WHERE title IS NULL;

-- NOT NULL 제약조건 추가 (새 리뷰는 title 필수)
ALTER TABLE program_review
    ALTER COLUMN title SET NOT NULL;

-- 인덱스 추가 (검색 성능 향상)
CREATE INDEX idx_program_review_program_id ON program_review(program_id);
CREATE INDEX idx_program_review_member_id ON program_review(member_id);

COMMENT ON COLUMN program_review.title IS '리뷰 제목';