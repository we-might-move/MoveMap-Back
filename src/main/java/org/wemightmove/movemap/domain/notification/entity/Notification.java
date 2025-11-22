package org.wemightmove.movemap.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.global.entity.BaseTimeEntity;
import org.wemightmove.movemap.global.enums.DeviceType;

@Entity
@Getter
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_member", columnList = "member_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "fcm_token", length = 500, nullable = false)
    private String fcmToken;

    @Column(name = "device_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    // 기기 고유 ID - 같은 기기 중복 등록 방지
    @Column(name = "device_id", length = 100)
    private String deviceId;

    // 푸시 알림 허용 여부
    @Column(name = "is_push_enabled", nullable = false)
    private boolean isPushEnabled = true;

    @Builder
    public Notification(Member member, String fcmToken, DeviceType deviceType, String deviceId) {
        this.member = member;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updatePushEnabled(boolean enabled) {
        this.isPushEnabled = enabled;
    }
}