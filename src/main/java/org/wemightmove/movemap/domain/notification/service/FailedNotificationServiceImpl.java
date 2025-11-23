package org.wemightmove.movemap.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.notification.dto.response.FailedPushMessageResponse;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailedNotificationServiceImpl implements FailedNotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String FAILED_PUSH_KEY = "push:failed:queue";
    private static final Duration TTL = Duration.ofHours(6);

    // 실패한 푸시 저장
    @Override
    public void saveFailedPush(Long memberId, String fcmToken, DeviceType deviceType, PushMessageResponse pushMessageResponse, String errorCode) {
        try {
            FailedPushMessageResponse failed = FailedPushMessageResponse.create(
                    memberId, fcmToken, deviceType.name(), pushMessageResponse, errorCode
            );

            String json = objectMapper.writeValueAsString(failed);

            redisTemplate.opsForList().leftPush(FAILED_PUSH_KEY, json);
            redisTemplate.expire(FAILED_PUSH_KEY, TTL);

            log.info("실패한 푸시 저장");

        } catch (JsonProcessingException e) {
            log.error("실패한 푸시 저장 실패 - memberId: {}, error: {}", memberId, e.getMessage(), e);
        }
    }

    /**
     * 실패한 푸시 하나 꺼내기(오래된 것부터 RPOP)
     * @return
     */
    @Override
    public FailedPushMessageResponse popFailedPush() {
        String json = redisTemplate.opsForList().rightPop(FAILED_PUSH_KEY);

        if(json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, FailedPushMessageResponse.class);
        } catch (JsonProcessingException e) {
            log.error("실패한 푸시 조회 실패 - error: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void requeueFailedPush(FailedPushMessageResponse failedPushMessageResponse) {
        try {
            // withIncrementRetry() 로 새 객체 생성
            FailedPushMessageResponse incremented = failedPushMessageResponse.withIncrementedRetry();
            String json = objectMapper.writeValueAsString(incremented);
            redisTemplate.opsForList().leftPush(FAILED_PUSH_KEY, json);
        } catch (JsonProcessingException e) {
            log.error("재큐잉 실패 - memberId: {}, error: {}", failedPushMessageResponse.memberId(), e.getMessage(), e);
        }
    }

    @Override
    public long getQueueSize() {
        Long size = redisTemplate.opsForList().size(FAILED_PUSH_KEY);
        return size != null ? size : 0;
    }
}
