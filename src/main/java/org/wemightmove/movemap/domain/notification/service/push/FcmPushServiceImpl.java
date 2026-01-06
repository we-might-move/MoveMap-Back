package org.wemightmove.movemap.domain.notification.service.push;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.domain.notification.entity.Notification;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.exception.FcmRetryableException;

import java.util.List;

@Slf4j
//@Service
@RequiredArgsConstructor
public class FcmPushServiceImpl implements PushService {

    private final NotificationRepository notificationRepository;
    private final PushSenderService pushSenderService;


    // 사용자에게 푸시 발송(비동기) : 별도 스레드에서 실행 - API 응답에 영향 없음
    @Override
    @Async("pushExecutor")
    public void sendToMember(Long memberId, PushMessageResponse pushMessageResponse) {
        List<Notification> devices = notificationRepository.findByMemberIdAndIsPushEnabledTrue(memberId);

        if (devices.isEmpty()) {
            log.debug("등록된 기기 없음 - memberId : {}", memberId);
            return;
        }

        for (Notification device : devices) {
            pushSenderService.sendWithRetry(memberId, device.getFcmToken(), device.getDeviceType(), pushMessageResponse);
        }
    }
}
