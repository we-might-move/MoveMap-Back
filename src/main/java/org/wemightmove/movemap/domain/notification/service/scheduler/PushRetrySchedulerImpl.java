package org.wemightmove.movemap.domain.notification.service.scheduler;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.domain.notification.dto.response.FailedPushMessageResponse;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
//@Component
@RequiredArgsConstructor
public class PushRetrySchedulerImpl implements PushRetryScheduler {

    private final FailedNotificationService failedNotificationService;
    private final NotificationRepository notificationRepository;

    @Value("${push.retry.max-attempts:5}")
    private int maxRetryAttempts;

    @Value("${push.retry.batch-size:50}")
    private int batchSize;

    /**
     * THINK : 5분 간격으로 정한 이유는?
     * 실패한 푸시 재시도(5분 마다)
     */
    @Scheduled(fixedDelayString = "${push.retry.schedule-rate:300000}")
    @Override
    public void retryFailedPushMessages() {
        long queueSize = failedNotificationService.getQueueSize();

        if (queueSize == 0) return;

        log.info("실패한 푸시 재시도 시작 - 큐 크기: {}", queueSize);

        int processed = 0;
        int success = 0;
        int failed = 0;
        int discarded = 0;

        while (processed < batchSize) {
            FailedPushMessageResponse failedPush = failedNotificationService.popFailedPush();

            if (failedPush == null) {
                break;
            }

            processed++;

            // 재시도 횟수 초과
            if (failedPush.isMaxRetryExceeded(maxRetryAttempts)) {
                log.warn("최대 재시도 초과, 폐기 - memberId: {}, retryCount: {}",
                        failedPush.memberId(), failedPush.retryCount());
                discarded++;
                continue;
            }

            // 재전송 시도
            boolean sent = retrySend(failedPush);

            if (sent) {
                success++;
            } else {
                // 실패하면 다시 큐에
                failedNotificationService.requeueFailedPush(failedPush);
                failed++;
            }
        }

        log.info("실패한 푸시 재시도 완료 - 처리: {}, 성공: {}, 실패: {}, 폐기: {}",
                processed, success, failed, discarded);


    }

    private boolean retrySend(FailedPushMessageResponse failedPush) {
        try {
            PushMessageResponse pushMessage = failedPush.pushMessageResponse();

            Message message = Message.builder()
                    .setToken(failedPush.fcmToken())
                    .setNotification(Notification.builder()
                            .setTitle(pushMessage.title())
                            .setBody(pushMessage.body())
                            .build())
                    .putAllData(pushMessage.data())
                    .putData("type", pushMessage.notificationType())
                    .build();

            FirebaseMessaging.getInstance().send(message);

            log.info("재시도 성공 - memberId: {}, retryCount: {}",
                    failedPush.memberId(), failedPush.retryCount());
            return true;

        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();

            // 토큰 무효 → 삭제, 재시도 안 함
            if (errorCode == MessagingErrorCode.UNREGISTERED ||
                    errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                log.info("재시도 중 무효 토큰 발견, 삭제 - fcmToken: {}...",
                        failedPush.fcmToken().substring(0, 20));
                notificationRepository.deleteByFcmToken(failedPush.fcmToken());
                return true;  // 큐에 다시 안 넣음
            }

            log.warn("재시도 실패 - memberId: {}, error: {}",
                    failedPush.memberId(), errorCode);
            return false;
        }
    }
}
