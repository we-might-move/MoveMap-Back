package org.wemightmove.movemap.domain.member.repository;

import org.wemightmove.movemap.domain.member.dto.response.FavoriteProgramResponse;

import java.math.BigDecimal;
import java.util.List;

public interface MemberProgramRepositoryCustom {
    List<FavoriteProgramResponse> findFavoriteProgramsByMemberIdWithDistance(
            Long memberId,
            BigDecimal currentLatitude,
            BigDecimal currentLongitude,
            Long cursor,
            int size
    );
    List<FavoriteProgramResponse> findFavoriteProgramsByMemberId(Long memberId, Long cursor, int size);
}
