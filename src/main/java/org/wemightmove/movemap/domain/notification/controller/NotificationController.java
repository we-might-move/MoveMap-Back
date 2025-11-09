package org.wemightmove.movemap.domain.notification.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Notification")
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
}
