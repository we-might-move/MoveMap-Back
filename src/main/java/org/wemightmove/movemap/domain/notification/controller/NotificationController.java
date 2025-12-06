package org.wemightmove.movemap.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.notification.dto.request.DeviceRegisterRequest;
import org.wemightmove.movemap.domain.notification.service.NotificationService;
import org.wemightmove.movemap.global.security.CustomUserDetails;

@RestController
@Tag(name = "Notification")
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "알림 토큰 등록",
            description = """
            EXPO 토큰을 등록합니다.
            등록 예시
            - fcmToken : ExponentPushToken[-nCUSiBPR6D3qA26LDJ4rT]
            - deviceType : ANDROID / IOS
            - deviceId : 기기 고유 번호
            """
    )
    @PostMapping
    public ResponseEntity<Void> registerDevice(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @RequestBody DeviceRegisterRequest request) {

        notificationService.registerDevice(member.getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "알림 허용 여부 수정")
    @PatchMapping("/{deviceId}/push")
    public ResponseEntity<Void> updatePushEnabled(
            @AuthenticationPrincipal CustomUserDetails member,
            @PathVariable String deviceId,
            @RequestParam boolean enabled) {

        notificationService.updatePushEnabled(member.getId(), deviceId, enabled);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "알림 기기 등록 해제", description = "deviceId : 기기의 고유 번호")
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> unregisterDevice(
            @AuthenticationPrincipal CustomUserDetails member,
            @PathVariable String deviceId) {
        notificationService.unregisterDevice(member.getId(), deviceId);
        return ResponseEntity.ok().build();
    }
}
