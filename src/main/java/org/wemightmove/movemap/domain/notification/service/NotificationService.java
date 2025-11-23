package org.wemightmove.movemap.domain.notification.service;

import org.wemightmove.movemap.domain.notification.dto.request.DeviceRegisterRequest;

public interface NotificationService {
    void registerDevice(Long memberId, DeviceRegisterRequest request);
    void updatePushEnabled(Long memberId, String deviceId, boolean enabled);
    void unregisterDevice(Long memberId, String deviceId);
    void deleteDevice(String fcmToken);
}
