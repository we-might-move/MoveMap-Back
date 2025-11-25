package org.wemightmove.movemap.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.domain.notification.entity.Notification;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.exception.FcmRetryableException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushServiceImpl implements FcmPushService {

    private final NotificationRepository notificationRepository;
    private final FailedNotificationService failedNotificationService;
    private final NotificationService notificationService;

    // 사용자에게 푸시 발송(비동기) : 별도 스레드에서 실행 - API 응답에 영향 없음
    @Override
    @Async("pushExecutor")
    public void sendToMember(Long memberId, PushMessageResponse pushMessageResponse) {
        List<Notification> devices = notificationRepository.findByMemberIdAndIsPushEnabledTrue(memberId);

        if (devices.isEmpty()) {
            log.debug("등록된 기기 없음 - memberId : {}", memberId);
            return;
        }

        for (Notification device : devices) {
            sendWithRetry(memberId, device.getFcmToken(), device.getDeviceType(), pushMessageResponse);
        }
    }

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

    @Override
    @Recover
    public void recoverFailedPush(FcmRetryableException e, Long memberId, String fcmToken, DeviceType deviceType, PushMessageResponse pushMessageResponse) {
        log.error("푸시 전송 최종 실패");

        failedNotificationService.saveFailedPush(memberId, fcmToken, deviceType, pushMessageResponse, e.getErrorCode());
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
