package org.wemightmove.movemap.domain.record.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.domain.member.entity.Member;

import java.time.LocalDate;

@Entity
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_steps_record_member_date", columnNames = {"member_id", "date"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StepsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "count")
    private int count;

    @Column(name = "distance")
    private double distance;
}
