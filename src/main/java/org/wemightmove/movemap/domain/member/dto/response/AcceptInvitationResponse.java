package org.wemightmove.movemap.domain.member.dto.response;

public record AcceptInvitationResponse(
        Long parentId,
        String parentName,
        String parentRole
) {
}
