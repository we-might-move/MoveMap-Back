package org.wemightmove.movemap.domain.member.dto.response;

import java.util.List;

public record ChildListResponse(
        List<ChildResponse> children
) {

    public record ChildResponse(
            Long id,
            String nickname,
            String role
    ){
    }
}
