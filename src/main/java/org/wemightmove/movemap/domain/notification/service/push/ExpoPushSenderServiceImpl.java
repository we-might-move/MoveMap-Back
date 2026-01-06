package org.wemightmove.movemap.domain.notification.service.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.notification.dto.request.ExpoPushRequest;
import org.wemightmove.movemap.domain.notification.dto.response.ExpoPushResponse;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.exception.ExpoRetryableException;
import org.wemightmove.movemap.domain.notification.service.NotificationService;
import org.wemightmove.movemap.domain.notification.service.scheduler.FailedNotificationService;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoPushSenderServiceImpl implements PushSenderService {

    // 상수
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient expoHttpClient;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final FailedNotificationService failedNotificationService;

    /**
     * Expo Push 전송 (자동 재시도)
     *
     * @Retryable 동작
     * 1. ExpoRetryableException 발생 시 재시도
     * 2. 최대 3번 시도 (첫 시도 + 2번 재시도)
     * 3. 재시도 간격: 1초 -> 2초 (exponential backoff)
     * 4. 3번 다 실패하면 @Recover 메서드 호출
     */
    @Retryable(
            retryFor = ExpoRetryableException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public void sendWithRetry(Long memberId, String expoPushToken, DeviceType deviceType, PushMessageResponse pushMessageResponse) {
        try {
            // Expo Push Token 유효성 검증
            if (!isValidExpoPushToken(expoPushToken)) {
                log.warn("유효하지 않은 Expo Push Token - memberId: {}", memberId);
                notificationService.deleteDevice(expoPushToken);
                throw new CustomException(ErrorCode.INVALID_FCM_TOKEN);
            }

            ExpoPushRequest request = ExpoPushRequest.of(
                    expoPushToken,
                    pushMessageResponse.title(),
                    pushMessageResponse.body(),
                    pushMessageResponse.data()
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
                    throw new ExpoRetryableException(
                            "Expo Push API 호출 실패",
                            "HTTP_ERROR_" + response.code(),
                            null
                    );
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                ExpoPushResponse expoPushResponse = objectMapper.readValue(responseBody, ExpoPushResponse.class);

                handleExpoPushResponse(expoPushToken, expoPushResponse);
            }

        } catch (IOException e) {
            log.error("Expo Push 전송 중 IO 에러 - memberId: {}", memberId, e);
            throw new ExpoRetryableException("네트워크 오류", "IO_ERROR", e);
        }
    }

    /**
     * 재시도 가능한 예외용 @Recover (ExpoRetryableException)
     * - 3회 재시도 후에도 실패한 경우
     * - Redis 큐에 저장하여 나중에 배치로 재시도
     */
    @Recover
    public void recoverRetryableException(
            ExpoRetryableException e,  // ← 구체적인 예외 타입!
            Long memberId,
            String expoPushToken,
            DeviceType deviceType,
            PushMessageResponse pushMessageResponse) {

        log.error("푸시 전송 최종 실패 (재시도 소진) - memberId: {}, error: {}",
                memberId, e.getErrorCode());

        // Redis에 저장 → 배치로 재시도
        failedNotificationService.saveFailedPush(
                memberId, expoPushToken, deviceType, pushMessageResponse, e.getErrorCode());
    }

    /**
     * 재시도 불가능한 예외용 @Recover (그 외 모든 예외)
     * - CustomException: 토큰 무효, 영구적 오류
     * - NullPointerException: 코드 버그
     * - Redis 저장 안함 (재시도 의미 없음)
     */
    @Recover
    public void recoverNonRetryableException(
            Exception e,  // ← 모든 예외 포괄
            Long memberId,
            String expoPushToken,
            DeviceType deviceType,
            PushMessageResponse pushMessageResponse) {

        log.warn("푸시 전송 실패 (재시도 불가) - memberId: {}, type: {}, message: {}",
                memberId, e.getClass().getSimpleName(), e.getMessage());

        // Redis에 저장하지 않음 (재시도 의미 없음)
    }

    /**
     * Expo Push Token 유효성 검증
     * 형식: ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]
     */
    private boolean isValidExpoPushToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return token.startsWith("ExponentPushToken[") && token.endsWith("]");
    }

    /**
     * Expo Push 응답 처리
     */
    private void handleExpoPushResponse(String expoPushToken, ExpoPushResponse response) {
        if (response.data() == null || response.data().isEmpty()) {
            throw new ExpoRetryableException("응답 데이터 없음", "EMPTY_RESPONSE", null);
        }

        ExpoPushResponse.ExpoPushTicket ticket = response.data().get(0);

        if (ticket.isSuccess()) {
            log.info("푸시 전송 성공 - ticketId: {}", ticket.id());
            return;
        }

        // 기기 미등록 에러 -> 토큰 삭제
        if (ticket.isDeviceNotRegistered()) {
            log.info("무효 토큰 삭제 - token: {}...", expoPushToken.substring(0, 30));
            notificationService.deleteDevice(expoPushToken);
            throw new CustomException(ErrorCode.INVALID_FCM_TOKEN);
        }

        // 재시도 가능한 에러
        if (ticket.isRetryable()) {
            String errorCode = ticket.details() != null ? ticket.details().error() : "UNKNOWN";
            log.warn("Expo Push 일시 오류, 재시도 예정 - error: {}", errorCode);
            throw new ExpoRetryableException("일시적 오류", errorCode, null);
        }

        // 기타 에러
        String errorMsg = ticket.message() != null ? ticket.message() : "UNKNOWN_ERROR";
        log.error("Expo Push 전송 실패 - error: {}", errorMsg);
        throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
    }
}
