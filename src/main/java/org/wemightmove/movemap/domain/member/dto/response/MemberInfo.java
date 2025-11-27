package org.wemightmove.movemap.domain.member.dto.response;

import java.time.LocalDateTime;

public record MemberInfo (
        Long id,
        String nickname,
        String role
) {
    public static MemberInfo of(Long id, String nickname, String role) {
        return new MemberInfo(id, nickname, role);
    }
}