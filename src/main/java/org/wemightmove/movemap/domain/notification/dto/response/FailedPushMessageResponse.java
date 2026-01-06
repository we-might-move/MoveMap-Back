package org.wemightmove.movemap.domain.notification.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record FailedPushMessageResponse(
        String uuid,
        Long memberId,
        String pushToken,
        String deviceType,
        PushMessageResponse pushMessageResponse,
        int retryCount,
        String errorCode,
        LocalDateTime createdAt,
        LocalDateTime lastRetryAt
) {
    /**
     * 새 실패 메시지 생성
     */
    public static FailedPushMessageResponse create(Long memberId, String pushToken, String deviceType,
                                           PushMessageResponse pushMessage, String errorCode) {
        return new FailedPushMessageResponse(
                UUID.randomUUID().toString(),
                memberId,
                pushToken,
                deviceType,
                pushMessage,
                0,
                errorCode,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * 재시도 횟수 증가한 새 객체 반환 (Record는 불변)
     */
    public FailedPushMessageResponse withIncrementedRetry() {
        return new FailedPushMessageResponse(
                this.uuid,
                this.memberId,
                this.pushToken,
                this.deviceType,
                this.pushMessageResponse,
                this.retryCount + 1,
                this.errorCode,
                this.createdAt,
                LocalDateTime.now()
        );
    }

    public boolean isMaxRetryExceeded(int maxRetry) {
        return this.retryCount >= maxRetry;
    }
}
