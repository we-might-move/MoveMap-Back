package org.wemightmove.movemap.domain.member.dto.response;

import java.util.List;

public record FavoriteProgramListResponse(
        List<FavoriteProgramResponse> programs,
        Long nextCursor,
        boolean hasNext
) {
}
