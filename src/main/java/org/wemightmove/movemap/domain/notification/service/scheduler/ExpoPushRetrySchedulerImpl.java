// PushRetrySchedulerImpl.java (수정)
package org.wemightmove.movemap.domain.notification.service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.domain.notification.dto.request.ExpoPushRequest;
import org.wemightmove.movemap.domain.notification.dto.response.ExpoPushResponse;
import org.wemightmove.movemap.domain.notification.dto.response.FailedPushMessageResponse;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpoPushRetrySchedulerImpl implements PushRetryScheduler {

    private final FailedNotificationService failedNotificationService;
    private final NotificationRepository notificationRepository;
    private final OkHttpClient expoHttpClient;
    private final ObjectMapper objectMapper;

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Value("${push.retry.max-attempts:5}")
    private int maxRetryAttempts;

    @Value("${push.retry.batch-size:50}")
    private int batchSize;

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

        // 실패한 메시지들을 모아서 루프 끝난 후 한번에 requeue (같은 실행에서 중복 처리 방지)
        java.util.List<FailedPushMessageResponse> toRequeue = new java.util.ArrayList<>();

        while (processed < batchSize) {
            FailedPushMessageResponse failedPush = failedNotificationService.popFailedPush();

            if (failedPush == null) break;

            processed++;

            // 최대 재시도 횟수 초과
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
                toRequeue.add(failedPush);
                failed++;
            }
        }

        // 실패한 메시지들 requeue (루프 끝난 후)
        for (FailedPushMessageResponse failedPush : toRequeue) {
            failedNotificationService.requeueFailedPush(failedPush);
        }

        log.info("실패한 푸시 재시도 완료 - 처리: {}, 성공: {}, 실패: {}, 폐기: {}",
                processed, success, failed, discarded);
    }

    private boolean retrySend(FailedPushMessageResponse failedPush) {
        try {
            PushMessageResponse pushMessage = failedPush.pushMessageResponse();

            ExpoPushRequest request = ExpoPushRequest.of(
                    failedPush.fcmToken(), // 실제로는 expoPushToken
                    pushMessage.title(),
                    pushMessage.body(),
                    pushMessage.data()
            );

            String jsonBody = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(jsonBody, JSON);

            Request httpRequest = new Request.Builder()
                    .url(EXPO_PUSH_URL)
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = expoHttpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("재시도 실패 - HTTP 상태: {}", response.code());
                    return false;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                ExpoPushResponse expoPushResponse = objectMapper.readValue(responseBody, ExpoPushResponse.class);

                if (expoPushResponse.data() == null || expoPushResponse.data().isEmpty()) {
                    return false;
                }

                ExpoPushResponse.ExpoPushTicket ticket = expoPushResponse.data().get(0);

                // 성공
                if (ticket.isSuccess()) {
                    log.info("재시도 성공 - memberId: {}, retryCount: {}",
                            failedPush.memberId(), failedPush.retryCount());
                    return true;
                }

                // 토큰 무효 -> 삭제
                if (ticket.isDeviceNotRegistered()) {
                    log.info("재시도 중 무효 토큰 발견, 삭제 - token: {}...",
                            failedPush.fcmToken().substring(0, 30));
                    notificationRepository.deleteByFcmToken(failedPush.fcmToken());
                    return true; // 큐에 다시 안 넣음
                }

                log.warn("재시도 실패 - memberId: {}, error: {}",
                        failedPush.memberId(), ticket.message());
                return false;
            }

        } catch (Exception e) {
            log.error("재시도 중 예외 발생 - memberId: {}", failedPush.memberId(), e);
            return false;
        }
    }
}