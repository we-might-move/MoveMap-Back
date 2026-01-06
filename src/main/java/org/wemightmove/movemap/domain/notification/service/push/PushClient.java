package org.wemightmove.movemap.domain.notification.service.push;

import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.exception.ExpoRetryableException;

public interface PushClient {
    void sendWithRetry(Long memberId, String pushToken, DeviceType deviceType, PushMessageResponse messageResponse);
    void recoverRetryableException(ExpoRetryableException e, Long memberId, String pushToken, DeviceType deviceType, PushMessageResponse messageResponse);
    void recoverNonRetryableException(Exception e, Long memberId, String pushToken, DeviceType deviceType, PushMessageResponse messageResponse);
}
