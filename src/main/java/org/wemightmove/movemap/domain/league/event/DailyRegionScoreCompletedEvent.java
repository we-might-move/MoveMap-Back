package org.wemightmove.movemap.domain.league.event;

import lombok.Builder;
import org.wemightmove.movemap.domain.member.entity.Member;

import java.time.LocalDate;

@Builder
public record DailyRegionScoreCompletedEvent (
    Member member,
    String regionCd,
    LocalDate date
) {
    }