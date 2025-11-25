package org.wemightmove.movemap.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.dto.request.FavoriteProgramRequest;
import org.wemightmove.movemap.domain.member.dto.response.FavoriteProgramListResponse;
import org.wemightmove.movemap.domain.member.dto.response.FavoriteProgramResponse;
import org.wemightmove.movemap.domain.member.repository.MemberProgramRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProgramQueryServiceImpl implements MemberProgramQueryService {

    private final MemberProgramRepository memberProgramRepository;

    @Override
    public FavoriteProgramListResponse getFavoritePrograms(Long memberId,
                                                           BigDecimal currentLatitude,
                                                           BigDecimal currentLongitude,
                                                           Long cursor,
                                                           Integer size) {

        List<FavoriteProgramResponse> programs = memberProgramRepository.findFavoriteProgramsByMemberId(
                memberId, currentLatitude, currentLongitude, cursor, size + 1
        );

        boolean hasNext = programs.size() > size;
        if(hasNext) {
            programs = programs.subList(0, size);
        }

        Long nextCursor = programs.isEmpty() ? null : programs.get(programs.size() - 1).memberProgramId();

        return new FavoriteProgramListResponse(programs, nextCursor, hasNext);
    }
}
