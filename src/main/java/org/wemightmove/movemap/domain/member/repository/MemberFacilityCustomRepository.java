package org.wemightmove.movemap.domain.member.repository;

import org.wemightmove.movemap.domain.member.dto.response.FavoriteFacilityResponse;

import java.math.BigDecimal;
import java.util.List;

public interface MemberFacilityCustomRepository {

    List<FavoriteFacilityResponse> findFavoriteFacilityWithDistance(
            Long memberId,
            BigDecimal currentLatitude,
            BigDecimal currentLongitude,
            Long cursor,
            int size
    );
}
