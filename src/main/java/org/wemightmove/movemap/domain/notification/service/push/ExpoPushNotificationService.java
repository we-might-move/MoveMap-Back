package org.wemightmove.movemap.domain.notification.service.push;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.domain.notification.entity.Notification;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoPushNotificationService implements PushNotificationService {

    private final NotificationRepository notificationRepository;
    private final PushClient pushClient;

    @Override
    @Async("pushExecutor")
    public void sendToMember(Long memberId, PushMessageResponse pushMessageResponse) {
        List<Notification> devices = notificationRepository.findByMemberIdAndIsPushEnabledTrue(memberId);

        if (devices.isEmpty()) {
            log.debug("등록된 기기 없음 - memberId: {}", memberId);
            return;
        }

        for (Notification device : devices) {
            pushClient.sendWithRetry(memberId, device.getFcmToken(), device.getDeviceType(), pushMessageResponse);
        }
    }
}