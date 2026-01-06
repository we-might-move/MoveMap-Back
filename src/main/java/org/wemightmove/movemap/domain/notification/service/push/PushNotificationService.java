package org.wemightmove.movemap.domain.notification.service.push;

import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;

public interface PushNotificationService {
    void sendToMember(Long memberId, PushMessageResponse pushMessageResponse);
}
