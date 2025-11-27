package org.wemightmove.movemap.domain.member.service;

import org.wemightmove.movemap.domain.member.dto.response.ReceivedInviteResponse;
import org.wemightmove.movemap.domain.member.dto.response.SentInviteResponse;

public interface MemberQueryService {

    SentInviteResponse getSentInviteList(Long parentId);

    ReceivedInviteResponse getReceivedInviteList(Long childId);
}
