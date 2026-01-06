package org.wemightmove.movemap.domain.notification.service;

import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;

public interface PushSenderService {
    void sendWithRetry(Long memberId, String pushToken, DeviceType deviceType, PushMessageResponse messageResponse);
    void recoverFailedPush(RuntimeException e, Long memberId, String pushToken, DeviceType deviceType, PushMessageResponse messageResponse);
}
