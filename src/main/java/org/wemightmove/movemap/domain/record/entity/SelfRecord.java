package org.wemightmove.movemap.domain.record.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.time.Duration;
import java.time.LocalDate;

@Entity
@Getter
@Table(name = "self_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelfRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", length = 50, nullable = false)
    private FacilityType exerciseType;

    @Column(name = "duration", columnDefinition = "interval", nullable = false)
    private Duration duration;
}
