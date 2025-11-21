package org.wemightmove.movemap.domain.member.service;

import org.wemightmove.movemap.domain.member.dto.request.AcceptInvitationRequest;
import org.wemightmove.movemap.domain.member.dto.request.RejectInvitationRequest;
import org.wemightmove.movemap.domain.member.dto.response.AcceptInvitationResponse;
import org.wemightmove.movemap.domain.member.dto.response.SendInviteResponse;

public interface MemberCommandService {
    SendInviteResponse sendInvite(Long parentId, String inviteCode);
    AcceptInvitationResponse acceptInvite(Long childId, AcceptInvitationRequest acceptInvitationRequest);
    void rejectInvte(Long childId, RejectInvitationRequest rejectInvitationRequest);

}
