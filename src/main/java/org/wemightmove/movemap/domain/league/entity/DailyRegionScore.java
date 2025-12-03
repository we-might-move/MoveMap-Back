package org.wemightmove.movemap.domain.league.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.global.entity.BaseTimeEntity;
import org.wemightmove.movemap.global.entity.RegionType;

import java.time.LocalDate;

@Entity
@Getter
@Table(
        name = "daily_region_score",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_daily_region_score_region_date", columnNames = {"region_id", "date"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRegionScore extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionType region;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "score")
    private int score;

    public DailyRegionScore(RegionType region, LocalDate date, int score) {
        this.region = region;
        this.date = date;
        this.score = score;
    }

    public void updateScore(int score) {
        this.score = score;
    }
}
