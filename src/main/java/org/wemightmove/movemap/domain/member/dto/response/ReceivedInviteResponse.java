package org.wemightmove.movemap.domain.member.dto.response;

import java.util.List;

public record ReceivedInviteResponse(
        String inviteCode,
        List<MemberInfo> myParents,
        List<InviteInfo> receivedInviteList
) {

    public static ReceivedInviteResponse of(String inviteCode, List<MemberInfo> myParents, List<InviteInfo> receivedInviteList) {
        return new ReceivedInviteResponse(inviteCode, myParents, receivedInviteList);
    }
}
