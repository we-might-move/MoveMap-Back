-- ============================================
-- Notification 테이블에 푸시 알림 관련 칼럼 추가
-- 기존 데이터 보존하며 안전하게 마이그레이션
-- ============================================

-- Step 1: 칼럼 추가 (NULL 허용)
-- IF NOT EXISTS로 멱등성 보장
ALTER TABLE notification
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS member_id BIGINT,
    ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(500),
    ADD COLUMN IF NOT EXISTS device_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS device_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS is_push_enabled BOOLEAN;

-- Step 2: 기존 데이터에 기본값 설정
-- created_at, updated_at이 NULL인 경우 현재 시간으로
UPDATE notification
SET created_at = NOW()
WHERE created_at IS NULL;

UPDATE notification
SET updated_at = NOW()
WHERE updated_at IS NULL;

-- is_push_enabled가 NULL인 경우 true로
UPDATE notification
SET is_push_enabled = true
WHERE is_push_enabled IS NULL;

-- Step 3: 기존 row 중 member_id나 fcm_token이 NULL인 경우 처리
-- 옵션 1: 삭제 (빈 row라면)
DELETE FROM notification
WHERE (member_id IS NULL OR fcm_token IS NULL)
  AND id IS NOT NULL;

-- Step 4: NOT NULL 제약조건 추가
-- 기존 데이터 정리 후 안전하게 추가
DO $$
    BEGIN
        -- member_id NOT NULL
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'notification'
              AND column_name = 'member_id'
              AND is_nullable = 'YES'
        ) THEN
            ALTER TABLE notification ALTER COLUMN member_id SET NOT NULL;
        END IF;

        -- fcm_token NOT NULL
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'notification'
              AND column_name = 'fcm_token'
              AND is_nullable = 'YES'
        ) THEN
            ALTER TABLE notification ALTER COLUMN fcm_token SET NOT NULL;
        END IF;

        -- device_type NOT NULL
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'notification'
              AND column_name = 'device_type'
              AND is_nullable = 'YES'
        ) THEN
            ALTER TABLE notification ALTER COLUMN device_type SET NOT NULL;
        END IF;

        -- is_push_enabled NOT NULL
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'notification'
              AND column_name = 'is_push_enabled'
              AND is_nullable = 'YES'
        ) THEN
            ALTER TABLE notification ALTER COLUMN is_push_enabled SET NOT NULL;
        END IF;
    END $$;

-- Step 5: 외래키 제약조건 추가 (중복 방지)
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_notification_member'
        ) THEN
            ALTER TABLE notification
                ADD CONSTRAINT fk_notification_member
                    FOREIGN KEY (member_id)
                        REFERENCES member(id)
                        ON DELETE CASCADE;
        END IF;
    END $$;

-- Step 6: 인덱스 추가 (중복 방지)
CREATE INDEX IF NOT EXISTS idx_notification_member
    ON notification(member_id);

CREATE INDEX IF NOT EXISTS idx_notification_fcm_token
    ON notification(fcm_token);

CREATE INDEX IF NOT EXISTS idx_notification_member_device
    ON notification(member_id, device_id);

-- Step 7: 칼럼 코멘트 추가 (문서화)
COMMENT ON COLUMN notification.member_id IS '회원 ID';
COMMENT ON COLUMN notification.fcm_token IS 'Firebase Cloud Messaging 토큰';
COMMENT ON COLUMN notification.device_type IS '기기 타입: ANDROID, IOS';
COMMENT ON COLUMN notification.device_id IS '기기 고유 식별자';
COMMENT ON COLUMN notification.is_push_enabled IS '푸시 알림 활성화 여부';
COMMENT ON COLUMN notification.created_at IS '생성 일시';
COMMENT ON COLUMN notification.updated_at IS '수정 일시';