package org.wemightmove.movemap.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.global.entity.BaseTimeEntity;

import java.time.LocalDate;

@Entity
@Getter
@Table(
        name = "member_score",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_score_member_date", columnNames = {"member_id", "date"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberScore extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    /** 원시 데이터 (서비스에서 업데이트) */
    @Column(name = "total_self_duration", nullable = false)
    private int totalSelfDuration;

    @Column(name = "total_steps", nullable = false)
    private int totalSteps;

    @Column(name = "total_checkin_duration", nullable = false)
    private int totalCheckinDuration;

    /** 자동 계산 칼럼 (DB에서 계산됨) */
    @Column(name = "self_score", insertable = false, updatable = false)
    private int selfScore;

    @Column(name = "steps_score", insertable = false, updatable = false)
    private int stepsScore;

    @Column(name = "checkin_score", insertable = false, updatable = false)
    private int checkinScore;

    /** 엔티티에만 존재하는 필드 **/
    @Transient
    private int totalScore;

    @Builder
    public MemberScore(Member member, LocalDate date) {
        this.member = member;
        this.date = date;
        this.totalSelfDuration = 0;
        this.totalSteps = 0;
        this.totalCheckinDuration = 0;
    }

    public void addSelfDuration(int minutes) {
        this.totalSelfDuration += minutes;
    }

    public void updateTotalSteps(int steps) {
        this.totalSteps = steps;
    }

    public void addCheckInDuration(int minutes) {
        this.totalCheckinDuration += minutes;
    }

    public int getTotalScore() {
        return this.selfScore + this.stepsScore + this.checkinScore;
    }

}
