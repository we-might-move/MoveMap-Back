package org.wemightmove.movemap.domain.notification.dto.response;

import org.wemightmove.movemap.global.enums.NotificationType;

import java.util.Map;

public record PushMessageResponse(
        String title,
        String body,
        String notificationType,
        Map<String, String> data
) {
    public PushMessageResponse {
        if (data == null) {
            data = Map.of();
        }
    }

    // 정적 메서드 : 알림 별로 생성할 수 있도록
    public static PushMessageResponse inviteResponse(String senderName, String inviteCode) {
        return new PushMessageResponse(
                "연결 요청",
                senderName + "님이 연결을 요청했습니다.",
                NotificationType.INVITE_REQUEST.name(),
                Map.of(
                        "inviteCode", inviteCode,
                        "senderName", senderName
                )
        );
    }

    public static PushMessageResponse invitedAccepted(String accepterName) {
        return new PushMessageResponse(
                "연결 수락",
                accepterName + "님이 연결 요청을 수락했습니다.",
                NotificationType.INVITE_ACCEPTED.name(),
                Map.of("accepterName", accepterName)
        );
    }

    public static PushMessageResponse inviteRejected(String rejecterName) {
        return new PushMessageResponse(
                "연결 거절",
                rejecterName + "님이 연결 요청을 거절했습니다.",
                NotificationType.INVITE_REJECTED.name(),
                Map.of("rejecterName", rejecterName)
        );
    }

    public static PushMessageResponse checkInReminder(String facilityName, Long facilityId) {
        return new PushMessageResponse(
                "체크인 알림",
                facilityName + " 근처입니다. 체크인 하시겠어요?",
                NotificationType.CHECK_IN_REMINDER.name(),
                Map.of(
                        "facilityName", facilityName,
                        "facilityId", String.valueOf(facilityId)
                )
        );
    }

    public static PushMessageResponse checkOutReminder(String facilityName, Long facilityId) {
        return new PushMessageResponse(
                "체크아웃 알림",
                facilityName + "에서 운동 끝나셨나요? 체크아웃 해주세요!",
                NotificationType.CHECK_OUT_REMINDER.name(),
                Map.of(
                        "facilityName", facilityName,
                        "facilityId", String.valueOf(facilityId)
                )
        );
    }
}
