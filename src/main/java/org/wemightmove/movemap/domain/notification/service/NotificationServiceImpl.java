package org.wemightmove.movemap.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.notification.dto.request.DeviceRegisterRequest;
import org.wemightmove.movemap.domain.notification.entity.Notification;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    // FCM 토큰 등록/갱신
    @Override
    @Transactional
    public void registerDevice(Long memberId, DeviceRegisterRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        notificationRepository.findByMemberIdAndDeviceId(memberId, request.deviceId())
                .ifPresentOrElse(
                        // 기존 기기 -> 토큰만 갱신
                        device -> {
                            device.updateFcmToken(request.fcmToken());
                            log.info("FCM 토큰 갱신");
                        },
                        // 새 기기 -> 등록
                        () -> {
                            Notification device = Notification.builder()
                                    .member(member)
                                    .fcmToken(request.fcmToken())
                                    .deviceType(getDeviceType(request.deviceType()))
                                    .deviceId(request.deviceId())
                                    .build();
                            notificationRepository.save(device);
                            log.info("새 기기 등록");
                        }
                );
    }

    @Override
    @Transactional
    public void updatePushEnabled(Long memberId, String deviceId, boolean enabled) {
        Notification device = notificationRepository.findByMemberIdAndDeviceId(memberId, deviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));
        device.updatePushEnabled(enabled);
        log.info("푸시 설정 변경");
    }

    /**
     * 기기 등록 해제(로그아웃)
     */
    @Override
    @Transactional
    public void unregisterDevice(Long memberId, String deviceId) {
        notificationRepository.deleteByMemberIdAndDeviceId(memberId, deviceId);
        log.info("기기 등록 해제");
    }

    @Override
    @Transactional
    public void deleteDevice(String fcmToken) {
        notificationRepository.deleteByFcmToken(fcmToken);
    }

    private DeviceType getDeviceType(String type) {
        return "IOS".equals(type) ? DeviceType.IOS : DeviceType.ANDROID;
    }
}
