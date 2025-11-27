UPDATE member
SET
    age = COALESCE(age, 20),
    sex = COALESCE(sex, 'OTHER')
WHERE age IS NULL OR sex IS NULL;

-- Verify all records have values
DO $$
    DECLARE
        null_count INTEGER;
    BEGIN
        SELECT COUNT(*) INTO null_count
        FROM member
        WHERE age IS NULL OR sex IS NULL;

        IF null_count > 0 THEN
            RAISE EXCEPTION 'Still % records with NULL age or sex', null_count;
        END IF;

        RAISE NOTICE 'Successfully updated all records. age and sex are ready for NOT NULL constraint.';
    END $$;