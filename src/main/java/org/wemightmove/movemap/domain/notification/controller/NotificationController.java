package org.wemightmove.movemap.domain.notification.controller;

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

    @PostMapping
    public ResponseEntity<Void> registerDevice(
            @AuthenticationPrincipal CustomUserDetails member,
            @Valid @RequestBody DeviceRegisterRequest request) {

        notificationService.registerDevice(member.getId(), request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{deviceId}/push")
    public ResponseEntity<Void> updatePushEnabled(
            @AuthenticationPrincipal CustomUserDetails member,
            @PathVariable String deviceId,
            @RequestParam boolean enabled) {

        notificationService.updatePushEnabled(member.getId(), deviceId, enabled);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> unregisterDevice(
            @AuthenticationPrincipal CustomUserDetails member,
            @PathVariable String deviceId) {
        notificationService.unregisterDevice(member.getId(), deviceId);
        return ResponseEntity.ok().build();
    }
}
