package org.wemightmove.movemap.domain.member.service;

import org.wemightmove.movemap.domain.member.dto.response.FavoriteFacilityPageResponse;

import java.math.BigDecimal;

public interface MemberFacilityQueryService {
    FavoriteFacilityPageResponse getFavoriteList(
            Long memberId,
            BigDecimal currentLatitude,
            BigDecimal currentLongitude,
            Long cursor,
            Integer size
    );
}
