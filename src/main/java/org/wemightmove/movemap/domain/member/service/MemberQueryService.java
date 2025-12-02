package org.wemightmove.movemap.domain.member.service;

import org.wemightmove.movemap.domain.member.dto.response.MemberInfoResponse;
import org.wemightmove.movemap.domain.member.dto.response.MemberScoreResponse;
import org.wemightmove.movemap.domain.member.dto.response.ReceivedInviteResponse;
import org.wemightmove.movemap.domain.member.dto.response.SentInviteResponse;

import java.time.LocalDate;

public interface MemberQueryService {

    SentInviteResponse getSentInviteList(Long parentId);

    ReceivedInviteResponse getReceivedInviteList(Long childId);

    MemberInfoResponse getMemberInfo(Long memberId);

    MemberScoreResponse getMemberScore(LocalDate date);
}
