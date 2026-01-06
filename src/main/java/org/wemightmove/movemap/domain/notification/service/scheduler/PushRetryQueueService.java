package org.wemightmove.movemap.domain.notification.service.scheduler;

import org.wemightmove.movemap.domain.notification.dto.response.FailedPushMessageResponse;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;

public interface PushRetryQueueService {
    void saveFailedPush(Long memberId, String pushToken, DeviceType deviceType, PushMessageResponse pushMessageResponse, String errorCode);
    FailedPushMessageResponse popFailedPush();
    void requeueFailedPush(FailedPushMessageResponse failedPushMessageResponse);
    long getQueueSize();
}
