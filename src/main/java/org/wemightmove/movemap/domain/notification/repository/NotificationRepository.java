package org.wemightmove.movemap.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.notification.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberIdAndIsPushEnabledTrue(Long memberId);
    Optional<Notification> findByMemberIdAndDeviceId(Long memberId, String deviceId);
    Optional<Notification> findByFcmToken(String fcmToken);
    void deleteByFcmToken(String fcmToken);
    void deleteByMemberIdAndDeviceId(Long memberId, String deviceId);
    boolean existsByMemberIdAndDeviceId(Long memberId, String deviceId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.member.id = :memberId")
    int deleteAllByMemberId(@Param("memberId") Long memberId);
}
