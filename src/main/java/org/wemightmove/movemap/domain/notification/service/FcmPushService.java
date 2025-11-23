package org.wemightmove.movemap.domain.notification.service;

import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.exception.FcmRetryableException;

public interface FcmPushService {
    void sendToMember(Long memberId, PushMessageResponse pushMessageResponse);
    void sendWithRetry(Long memberId, String fcmToken, DeviceType deviceType, PushMessageResponse pushMessageResponse);
    void recoverFailedPush(FcmRetryableException e, Long memberId, String fcmToken, DeviceType deviceType, PushMessageResponse pushMessageResponse);
}
