package org.wemightmove.movemap.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeviceRegisterRequest(
        @NotBlank(message = "FCM 토큰은 필수입니다")
        String fcmToken,
        @NotBlank(message = "기기 타입은 필수입니다(IOS/ANDROID)")
        String deviceType,
        @NotBlank(message = "기기 ID는 필수입니다")
        String deviceId
) {
}
