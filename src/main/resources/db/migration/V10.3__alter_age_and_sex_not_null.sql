-- Step 1: Alter columns to NOT NULL
ALTER TABLE member
    ALTER COLUMN age SET NOT NULL,
    ALTER COLUMN sex SET NOT NULL;

-- Step 2: Update check constraints (remove NULL checks)
ALTER TABLE member
    DROP CONSTRAINT chk_member_age,
    DROP CONSTRAINT chk_member_sex;

ALTER TABLE member
    ADD CONSTRAINT chk_member_age
        CHECK (age BETWEEN 1 AND 150);

ALTER TABLE member
    ADD CONSTRAINT chk_member_sex
        CHECK (sex IN ('MAN', 'WOMAN', 'OTHER'));

-- Step 3: Create indexes for performance
CREATE INDEX idx_member_age_sex
    ON member(age, sex)
    WHERE is_deleted = false;

CREATE INDEX idx_member_age
    ON member(age)
    WHERE is_deleted = false;

CREATE INDEX idx_member_sex
    ON member(sex)
    WHERE is_deleted = false;

-- Verification
DO $$
    DECLARE
        age_null_count INTEGER;
        sex_null_count INTEGER;
        total_count INTEGER;
    BEGIN
        SELECT COUNT(*) INTO total_count FROM member;
        SELECT COUNT(*) INTO age_null_count FROM member WHERE age IS NULL;
        SELECT COUNT(*) INTO sex_null_count FROM member WHERE sex IS NULL;

        RAISE NOTICE 'Migration completed successfully!';
        RAISE NOTICE 'Total members: %', total_count;
        RAISE NOTICE 'Members with NULL age: % (should be 0)', age_null_count;
        RAISE NOTICE 'Members with NULL sex: % (should be 0)', sex_null_count;
    END $$;