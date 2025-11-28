-- Step 1: Add columns as NULLABLE
ALTER TABLE member
    ADD COLUMN age INTEGER,
    ADD COLUMN sex VARCHAR(10);

-- Add check constraints (but columns are still nullable)
ALTER TABLE member
    ADD CONSTRAINT chk_member_age
        CHECK (age IS NULL OR (age BETWEEN 1 AND 150));

ALTER TABLE member
    ADD CONSTRAINT chk_member_sex
        CHECK (sex IS NULL OR sex IN ('MAN', 'WOMAN', 'OTHER'));

-- Add comment
COMMENT ON COLUMN member.age IS '회원 나이 (1-150)';
COMMENT ON COLUMN member.sex IS '회원 성별 (MALE, FEMALE, OTHER)';