package org.wemightmove.movemap.domain.member.service;

import org.wemightmove.movemap.domain.member.dto.response.SendInviteResponse;

public interface MemberCommandService {
    SendInviteResponse sendInvite(Long parentId, String inviteCode);
}
