package org.wemightmove.movemap.domain.league.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.global.entity.BaseTimeEntity;
import org.wemightmove.movemap.global.entity.RegionType;
import org.wemightmove.movemap.global.enums.LeagueType;

@Entity
@Getter
@Table(name = "league_status")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueStatus extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionType region;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private LeagueType type;
}
