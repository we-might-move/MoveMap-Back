-- ============================================
-- Notification 테이블 생성
-- ============================================

CREATE TABLE IF NOT EXISTS notification
(
    id              BIGSERIAL PRIMARY KEY,

    member_id       BIGINT                      NOT NULL,
    fcm_token       VARCHAR(500)                NOT NULL,
    device_type     VARCHAR(20)                 NOT NULL,
    device_id       VARCHAR(100),

    is_push_enabled BOOLEAN                     NOT NULL DEFAULT true,

    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================
-- 외래키 제약조건 추가
-- ============================================

ALTER TABLE notification
    ADD CONSTRAINT fk_notification_member
        FOREIGN KEY (member_id)
            REFERENCES member (id)
            ON DELETE CASCADE;

-- ============================================
-- 인덱스 추가
-- ============================================

CREATE INDEX IF NOT EXISTS idx_notification_member
    ON notification (member_id);

CREATE INDEX IF NOT EXISTS idx_notification_fcm_token
    ON notification (fcm_token);

CREATE INDEX IF NOT EXISTS idx_notification_member_device
    ON notification (member_id, device_id);

-- ============================================
-- 코멘트 추가
-- ============================================

COMMENT ON COLUMN notification.member_id IS '회원 ID';
COMMENT ON COLUMN notification.fcm_token IS 'Firebase Cloud Messaging 토큰';
COMMENT ON COLUMN notification.device_type IS '기기 타입: ANDROID, IOS';
COMMENT ON COLUMN notification.device_id IS '기기 고유 식별자';
COMMENT ON COLUMN notification.is_push_enabled IS '푸시 알림 활성화 여부';
COMMENT ON COLUMN notification.created_at IS '생성 일시';
COMMENT ON COLUMN notification.updated_at IS '수정 일시';