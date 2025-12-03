package org.wemightmove.movemap.domain.member.service;

import org.wemightmove.movemap.domain.member.dto.response.*;

import java.time.LocalDate;

public interface MemberQueryService {

    SentInviteResponse getSentInviteList(Long parentId);

    ReceivedInviteResponse getReceivedInviteList(Long childId);

    MemberInfoResponse getMemberInfo(Long memberId);

    MemberScoreResponse getMemberScore(LocalDate date);

    ChildListResponse getChildList();
}
