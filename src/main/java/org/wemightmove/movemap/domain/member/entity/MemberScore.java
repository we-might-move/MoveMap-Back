package org.wemightmove.movemap.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Column(name = "checkin_score")
    private int checkinScore;

    @Column(name = "steps_score")
    private int stepsScore;

    @Column(name = "self_score")
    private int selfScore;

    @Column(name = "total_score", insertable = false, updatable = false)
    private int totalScore;
}
