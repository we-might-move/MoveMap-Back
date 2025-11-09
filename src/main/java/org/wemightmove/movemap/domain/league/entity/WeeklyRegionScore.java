package org.wemightmove.movemap.domain.league.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.global.entity.BaseTimeEntity;
import org.wemightmove.movemap.global.entity.RegionType;

@Entity
@Getter
@Table(name = "weekly_region_score")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyRegionScore extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionType region;

    @Column(name = "year")
    private int year;

    @Column(name = "month")
    private int month;

    @Column(name = "week_number")
    private int weekNumber;

    @Column(name = "score")
    private int score;
}
