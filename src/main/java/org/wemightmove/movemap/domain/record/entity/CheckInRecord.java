package org.wemightmove.movemap.domain.record.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "check_in_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckInRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Builder
    public CheckInRecord(Member member, Facility facility, LocalDateTime checkInAt) {
        this.member = member;
        this.facility = facility;
        this.date = checkInAt.toLocalDate();
        this.checkInAt = checkInAt;
    }

    public void checkout(LocalDateTime checkOutAt) {
        if (checkOutAt.isBefore(this.checkInAt)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        this.checkOutAt = checkOutAt;
        this.durationMinutes = (int) Duration.between(checkInAt, checkOutAt).toMinutes();
    }
}
