-- =====================================================
-- 1단계: Generated Column만 추가
-- =====================================================
-- 실행 시간: 약 1-2분 (10만 건 기준)
-- 서비스 영향: 짧은 락 발생 (1-2분)
-- =====================================================

-- pg_trgm 확장 설치
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Generated Column 추가
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'program'
              AND column_name = 'name_normalized'
        ) THEN
            ALTER TABLE program
                ADD COLUMN name_normalized VARCHAR(200)
                    GENERATED ALWAYS AS (REPLACE(name, ' ', '')) STORED;

            RAISE NOTICE 'name_normalized 컬럼 추가 완료';
        ELSE
            RAISE NOTICE 'name_normalized 컬럼이 이미 존재함';
        END IF;
    END $$;