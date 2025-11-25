package org.wemightmove.movemap.domain.notification.service;

import org.wemightmove.movemap.domain.notification.dto.response.FailedPushMessageResponse;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;

public interface FailedNotificationService {
    void saveFailedPush(Long memberId, String fcmToken, DeviceType deviceType, PushMessageResponse pushMessageResponse, String errorCode);
    FailedPushMessageResponse popFailedPush();
    void requeueFailedPush(FailedPushMessageResponse failedPushMessageResponse);
    long getQueueSize();
}
