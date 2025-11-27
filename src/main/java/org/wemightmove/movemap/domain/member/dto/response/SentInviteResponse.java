package org.wemightmove.movemap.domain.member.dto.response;

import java.util.List;

public record SentInviteResponse(
        List<MemberInfo> myChildren,
        List<InviteInfo> sentInviteList
) {
    public static SentInviteResponse of(List<MemberInfo> myChildren, List<InviteInfo> sentInviteList) {
        return new SentInviteResponse(myChildren, sentInviteList);
    }
}
