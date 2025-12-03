package org.wemightmove.movemap.domain.member.event;

import lombok.Builder;
import org.wemightmove.movemap.domain.member.entity.Member;

import java.time.LocalDate;

@Builder
public record MemberScoreUpdatedEvent(
        Member member,
        String regionCd,
        LocalDate date
) {
}
