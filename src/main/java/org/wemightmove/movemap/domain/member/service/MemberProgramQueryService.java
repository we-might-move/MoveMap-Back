package org.wemightmove.movemap.domain.member.service;

import org.wemightmove.movemap.domain.member.dto.response.FavoriteProgramListResponse;

import java.math.BigDecimal;

public interface MemberProgramQueryService {
    FavoriteProgramListResponse getFavoritePrograms(
            Long memberId,
            BigDecimal currentLatitude,
            BigDecimal currentLongitude,
            Long cursor,
            Integer size
    );
}
