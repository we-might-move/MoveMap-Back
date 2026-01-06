package org.wemightmove.movemap.domain.notification.service;

import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;
import org.wemightmove.movemap.domain.notification.service.push.PushService;
import org.wemightmove.movemap.domain.notification.service.scheduler.FailedNotificationService;
import org.wemightmove.movemap.domain.notification.service.scheduler.PushRetryScheduler;
import org.wemightmove.movemap.global.support.IntegrationTestSupport;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SqlGroup({
        @Sql(value = "/sql/notification-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
class PushServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private PushService pushService;

    @Autowired
    private FailedNotificationService failedNotificationService;

    @Autowired
    private PushRetryScheduler pushRetryScheduler;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private OkHttpClient expoHttpClient;

    private Call mockCall;

    private static final String FAILED_PUSH_KEY = "push:failed:queue";

    @BeforeEach
    void setUp() throws IOException {
        // Mock 초기화 (이전 테스트 상태 제거)
        reset(expoHttpClient);

        // 새 Mock 설정
        mockCall = mock(Call.class);
        when(expoHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        mockExpoSuccessResponse();

        // Redis 큐 초기화
        redisTemplate.delete(FAILED_PUSH_KEY);
    }

    @AfterEach
    void tearDown() {
        // Redis 정리
        redisTemplate.delete(FAILED_PUSH_KEY);
    }

// ==================== 정상 전송 ====================

    @Test
    void 사용자에게_푸시를_전송할_수_있다() throws Exception {
        // given
        Long memberId = 1L;  // device-001, device-002 등록됨
        PushMessageResponse message = PushMessageResponse.inviteResponse("부모님", "invite-code");

        mockExpoSuccessResponse();

        // when
        pushService.sendToMember(memberId, message);

        // then - 비동기이므로 대기
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(expoHttpClient, atLeastOnce()).newCall(any(Request.class));
        });

        // Redis 큐에 저장 안됨 (성공했으니까)
        assertThat(failedNotificationService.getQueueSize()).isZero();
    }

    @Test
    void 등록된_기기가_없으면_전송하지_않는다() throws Exception {
        // given
        Long memberIdWithNoDevice = 999L;  // 기기 없는 사용자
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");

        // when
        pushService.sendToMember(memberIdWithNoDevice, message);

        // then - API 호출 없음
        await().during(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(expoHttpClient, never()).newCall(any(Request.class));
        });
    }

    @Test
    void 푸시_비활성화된_기기에는_전송하지_않는다() throws Exception {
        // given - member_id=2는 is_push_enabled=false
        Long memberId = 2L;
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");

        // when
        pushService.sendToMember(memberId, message);

        // then
        await().during(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(expoHttpClient, never()).newCall(any(Request.class));
        });
    }

    // ==================== 재시도 (@Retryable) ====================

    @Test
    void 일시적_오류_시_3회_재시도_후_Redis에_저장된다() throws Exception {
        // given
        Long memberId = 1L;
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");

        // 모든 요청에서 일시적 오류
        mockExpoRetryableErrorResponse();

        // when
        pushService.sendToMember(memberId, message);

        // then - 3회 재시도 후 Redis에 저장 (약 1초+2초 대기)
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // @Retryable: 3회 시도
            verify(expoHttpClient, atLeast(3)).newCall(any(Request.class));
            // @Recover: Redis에 저장됨
            assertThat(failedNotificationService.getQueueSize()).isGreaterThan(0);
        });
    }

    @Test
    void 첫_번째_시도에서_성공하면_재시도하지_않는다() throws Exception {
        // given
        Long memberId = 1L;
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");

        mockExpoSuccessResponse();

        // when
        pushService.sendToMember(memberId, message);

        // then - API 1회만 호출 (재시도 없음)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            // 기기 2개 등록 = 2회 호출
            verify(expoHttpClient, atMost(2)).newCall(any(Request.class));
        });

        assertThat(failedNotificationService.getQueueSize()).isZero();
    }

    @Test
    void HTTP_500_에러_시_재시도한다() throws Exception {
        // given
        Long memberId = 1L;
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");

        // 500 에러
        when(mockCall.execute()).thenReturn(createResponse(500, "Internal Server Error"));

        // when
        pushService.sendToMember(memberId, message);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(expoHttpClient, atLeast(3)).newCall(any(Request.class));
            assertThat(failedNotificationService.getQueueSize()).isGreaterThan(0);
        });
    }

    @Test
    void 네트워크_오류_시_재시도한다() throws Exception {
        // given
        Long memberId = 1L;
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");

        // IOException
        when(mockCall.execute()).thenThrow(new IOException("Connection refused"));

        // when
        pushService.sendToMember(memberId, message);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(expoHttpClient, atLeast(3)).newCall(any(Request.class));
            assertThat(failedNotificationService.getQueueSize()).isGreaterThan(0);
        });
    }

    // ==================== 토큰 무효 처리 ====================

    @Test
    void DeviceNotRegistered_응답_시_토큰이_삭제된다() throws Exception {
        // given
        Long memberId = 1L;
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");

        mockExpoDeviceNotRegisteredResponse();

        long initialDeviceCount = notificationRepository.findByMemberIdAndIsPushEnabledTrue(memberId).size();

        // when
        pushService.sendToMember(memberId, message);

        // then - 토큰 삭제됨
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            long currentCount = notificationRepository.findByMemberIdAndIsPushEnabledTrue(memberId).size();
            assertThat(currentCount).isLessThan(initialDeviceCount);
        });
    }

    // ==================== 배치 재시도 (스케줄러) ====================

    @Test
    void 스케줄러가_Redis_큐의_메시지를_재시도하여_성공하면_큐에서_제거된다() throws Exception {
        // given - 즉시 재시도 실패 → Redis에 저장
        Long memberId = 1L;
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");

        mockExpoRetryableErrorResponse();
        pushService.sendToMember(memberId, message);

        // Redis에 저장될 때까지 대기
        await().atMost(10, TimeUnit.SECONDS).until(() ->
                failedNotificationService.getQueueSize() > 0
        );

        // 이제 성공 응답으로 변경
        reset(expoHttpClient);
        mockCall = mock(Call.class);
        when(expoHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        mockExpoSuccessResponse();

        // when - 스케줄러 수동 실행
        pushRetryScheduler.retryFailedPushMessages();

        // then - 큐 비어있음 (성공)
        assertThat(failedNotificationService.getQueueSize()).isZero();
    }

    @Test
    void 스케줄러_재시도_실패_시_다시_큐에_추가된다() throws Exception {
        // given - Redis에 직접 저장
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");
        failedNotificationService.saveFailedPush(
                1L, "ExponentPushToken[xxx]",
                org.wemightmove.movemap.global.enums.DeviceType.ANDROID,
                message, "ERROR"
        );
        assertThat(failedNotificationService.getQueueSize()).isEqualTo(1);

        mockExpoRetryableErrorResponse();

        // when
        pushRetryScheduler.retryFailedPushMessages();

        // then - 실패하여 다시 큐에 추가됨
        assertThat(failedNotificationService.getQueueSize()).isEqualTo(1);

        // retryCount 증가 확인
        var requeued = failedNotificationService.popFailedPush();
        assertThat(requeued.retryCount()).isEqualTo(1);
    }

    @Test
    void 최대_재시도_횟수_초과_시_메시지가_폐기된다() throws Exception {
        // given - retryCount가 이미 최대값인 메시지 생성
        PushMessageResponse message = PushMessageResponse.inviteResponse("테스트", "code");

        // 5번 실패 시뮬레이션
        failedNotificationService.saveFailedPush(
                1L, "ExponentPushToken[xxx]",
                org.wemightmove.movemap.global.enums.DeviceType.ANDROID,
                message, "ERROR"
        );

        for (int i = 0; i < 5; i++) {
            var failed = failedNotificationService.popFailedPush();
            failedNotificationService.requeueFailedPush(failed);
        }

        mockExpoRetryableErrorResponse();

        // when
        pushRetryScheduler.retryFailedPushMessages();

        // then - 폐기되어 큐가 비어있음
        assertThat(failedNotificationService.getQueueSize()).isZero();
    }

    @Test
    void 빈_큐에서_스케줄러를_실행해도_문제_없다() throws IOException {
        // given - 빈 큐

        // when
        pushRetryScheduler.retryFailedPushMessages();

        // then - 에러 없음, API 호출 없음
        verify(expoHttpClient, never()).newCall(any(Request.class));
    }

    // ==================== 헬퍼 메서드 ====================

    private void mockExpoSuccessResponse() throws IOException {
        String response = """
            {"data": [{"status": "ok", "id": "ticket-123"}]}
            """;
        // thenAnswer: 매번 새 Response 생성 (body는 한 번만 읽을 수 있음)
        when(mockCall.execute()).thenAnswer(invocation -> createResponse(200, response));
    }

    private void mockExpoRetryableErrorResponse() throws IOException {
        String response = """
            {"data": [{"status": "error", "details": {"error": "ServiceUnavailable"}}]}
            """;
        when(mockCall.execute()).thenAnswer(invocation -> createResponse(200, response));
    }

    private void mockExpoDeviceNotRegisteredResponse() throws IOException {
        String response = """
            {"data": [{"status": "error", "details": {"error": "DeviceNotRegistered"}}]}
            """;
        when(mockCall.execute()).thenAnswer(invocation -> createResponse(200, response));
    }

    private Response createResponse(int statusCode, String body) {
        return new Response.Builder()
                .request(new Request.Builder().url("https://exp.host/--/api/v2/push/send").build())
                .protocol(Protocol.HTTP_1_1)
                .code(statusCode)
                .message(statusCode == 200 ? "OK" : "Error")
                .body(ResponseBody.create(body, MediaType.get("application/json")))
                .build();
    }
}