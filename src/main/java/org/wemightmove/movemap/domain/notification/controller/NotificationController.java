package org.wemightmove.movemap.domain.notification.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.notification.dto.request.DeviceRegisterRequest;
import org.wemightmove.movemap.domain.notification.service.NotificationService;
import org.wemightmove.movemap.global.security.CustomUserDetails;

/**
 * FIXME : Auth 완성되면 memberId 부분 수정
 */
@RestController
@Tag(name = "Notification")
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> registerDevice(
            @RequestParam("memberId") Long memberId,
            @Valid @RequestBody DeviceRegisterRequest request) {

        notificationService.registerDevice(memberId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{deviceId}/push")
    public ResponseEntity<Void> updatePushEnabled(
            @RequestParam("memberId") Long memberId,
            @PathVariable String deviceId,
            @RequestParam boolean enabled) {

        notificationService.updatePushEnabled(memberId, deviceId, enabled);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> unregisterDevice(@RequestParam("memberId") Long memberId, @PathVariable String deviceId) {
        notificationService.unregisterDevice(memberId, deviceId);
        return ResponseEntity.ok().build();
    }
}
