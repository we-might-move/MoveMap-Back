package org.wemightmove.movemap.domain.member.dto.response;

import java.util.List;

public record FavoriteFacilityPageResponse(
        List<FavoriteFacilityResponse> content,
        Long nextCursor,
        boolean hasNext,
        int size
) {
}
