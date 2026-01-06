package org.wemightmove.movemap.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.exception.FcmRetryableException;

@Slf4j
//@Service
@RequiredArgsConstructor
public class FcmPushSenderServiceImpl implements PushSenderService {

    private final FailedNotificationService failedNotificationService;
    private final NotificationService notificationService;

    /**
     * FCM 전송(자동 재시도)
     *
     * @Retryable 동작
     * 1. FcmRetryableException 발생 시 재시도
     * 2. 최대 3번 시도 (첫 시도 + 2번 재시도)
     * 3. 재시도 간격 : 1초 --> 2초 (exponential backoff)
     * 4. 3번 다 실패하면 @Recover 메서드 호출
     */
    @Retryable(
            retryFor = FcmRetryableException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public void sendWithRetry(Long memberId, String fcmToken, DeviceType deviceType, PushMessageResponse pushMessageResponse) {
        try {
            Message message = buildMessage(fcmToken, deviceType, pushMessageResponse);
            String response = FirebaseMessaging.getInstance().send(message);

            log.info("푸시 전송 성공 - messageId : {}", response);
        } catch (FirebaseMessagingException e) {
            handleFcmException(fcmToken, e);
        }
    }

    /**
     * 재시도 가능한 예외용 @Recover (FcmRetryableException)
     * - 3회 재시도 후에도 실패한 경우
     * - Redis 큐에 저장하여 나중에 배치로 재시도
     */
    @Recover
    public void recoverRetryableException(
            FcmRetryableException e,
            Long memberId,
            String fcmToken,
            DeviceType deviceType,
            PushMessageResponse pushMessageResponse) {

        log.error("푸시 전송 최종 실패 (재시도 소진) - memberId: {}, error: {}",
                memberId, e.getErrorCode());

        failedNotificationService.saveFailedPush(
                memberId, fcmToken, deviceType, pushMessageResponse, e.getErrorCode());
    }

    /**
     * 재시도 불가능한 예외용 @Recover (그 외 모든 예외)
     * - CustomException: 토큰 무효, 영구적 오류
     * - NullPointerException: 코드 버그
     * - Redis 저장 안함 (재시도 의미 없음)
     */
    @Recover
    public void recoverNonRetryableException(
            Exception e,
            Long memberId,
            String fcmToken,
            DeviceType deviceType,
            PushMessageResponse pushMessageResponse) {

        log.warn("푸시 전송 실패 (재시도 불가) - memberId: {}, type: {}, message: {}",
                memberId, e.getClass().getSimpleName(), e.getMessage());

        // Redis에 저장하지 않음 (재시도 의미 없음)
    }

    // FCM 메시지 생성
    private Message buildMessage(String fcmToken, DeviceType deviceType, PushMessageResponse pushMessageResponse) {
        Message.Builder builder = Message.builder()
                .setToken(fcmToken)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(pushMessageResponse.title())
                        .setBody(pushMessageResponse.body()).build())
                .putAllData(pushMessageResponse.data())
                .putData("type", pushMessageResponse.notificationType());

        if(DeviceType.ANDROID.equals(deviceType)) {
            builder.setAndroidConfig(AndroidConfig.builder()
                    .setTtl(3600 * 1000)
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build());
        }

        if (DeviceType.IOS.equals(deviceType)) {
            builder.setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setSound("default")
                            .setBadge(1)
                            .build()).build());
        }

        return builder.build();
    }

    /**
     * FCM 에러 분류
     * 재시도 가능: UNAVAILABLE, INTERNAL, UNKNOWN 등 (일시적 오류)
     * 재시도 불가: UNREGISTERED, INVALID_ARGUMENT 등 (영구적 오류)
     */
    private void handleFcmException(String fcmToken, FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        String errorCodeStr = errorCode != null ? errorCode.name() : "UNKNOWN";

        // 재시도 불가능 에러
        if (isImpossibleError(errorCode)) {
            log.info("무효 토큰 삭제 - errorCode : {}", errorCodeStr);
            notificationService.deleteDevice(fcmToken);

            // 재시도 안 함
            throw new CustomException(ErrorCode.INVALID_FCM_TOKEN);
        }

        log.warn("FCM 일시 오류, 재시도 예정 - errorCode : {}", errorCodeStr);
        throw new FcmRetryableException("FCM 전송 실패", errorCodeStr, e);
    }

    private boolean isImpossibleError(MessagingErrorCode errorCode) {
        return MessagingErrorCode.UNREGISTERED.equals(errorCode) ||
                MessagingErrorCode.INVALID_ARGUMENT.equals(errorCode) ||
                MessagingErrorCode.SENDER_ID_MISMATCH.equals(errorCode);
    }
}
