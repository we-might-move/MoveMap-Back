package org.wemightmove.movemap.domain.notification.service;

import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.global.enums.DeviceType;

public interface PushSenderService {
    void sendWithRetry(Long memberId, String pushToken, DeviceType deviceType, PushMessageResponse messageResponse);
    // @Recover 메서드는 Spring Retry 프레임워크가 자동 호출하므로 인터페이스에 선언 불필요
}
