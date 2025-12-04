package org.wemightmove.movemap.domain.notification.service;

import org.springframework.retry.annotation.Recover;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.exception.ExpoRetryableException;
import org.wemightmove.movemap.global.exception.FcmRetryableException;

public interface PushService {
    void sendToMember(Long memberId, PushMessageResponse pushMessageResponse);
    void sendWithRetry(Long memberId, String fcmToken, DeviceType deviceType, PushMessageResponse pushMessageResponse);
    void recoverFailedPush(RuntimeException e, Long memberId, String token, DeviceType deviceType, PushMessageResponse pushMessageResponse);
}
