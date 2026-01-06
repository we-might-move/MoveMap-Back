package org.wemightmove.movemap.domain.notification.service.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.notification.dto.request.ExpoPushRequest;
import org.wemightmove.movemap.domain.notification.dto.response.ExpoPushResponse;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.domain.notification.entity.Notification;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.exception.ExpoRetryableException;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoPushServiceImpl implements PushService {

    private final NotificationRepository notificationRepository;
    private final PushSenderService pushSenderService;

    @Override
    @Async("pushExecutor")
    public void sendToMember(Long memberId, PushMessageResponse pushMessageResponse) {
        List<Notification> devices = notificationRepository.findByMemberIdAndIsPushEnabledTrue(memberId);

        if (devices.isEmpty()) {
            log.debug("등록된 기기 없음 - memberId: {}", memberId);
            return;
        }

        for (Notification device : devices) {
            pushSenderService.sendWithRetry(memberId, device.getFcmToken(), device.getDeviceType(), pushMessageResponse);
        }
    }
}