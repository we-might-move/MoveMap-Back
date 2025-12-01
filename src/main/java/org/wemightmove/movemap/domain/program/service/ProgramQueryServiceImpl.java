package org.wemightmove.movemap.domain.program.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;
import org.wemightmove.movemap.domain.program.repository.ProgramRepository;
import org.wemightmove.movemap.global.entity.RegionType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramQueryServiceImpl implements ProgramQueryService {

    private static final int DEFAULT_MAX_MARKERS = 10;

    private final MemberRepository memberRepository;
    private final ProgramRepository programRepository;
    private final RegionTypeRepository regionTypeRepository;

    @Override
    public ProgramMarkerResponse getMarkers(Long memberId) {
        Member member = getMember(memberId);

        RegionType regionType = regionTypeRepository.findChildRegionTypeByPrefix(member.getRegionCode()).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_DISTRICT));

        List<ProgramMarkerResponse.MarkerInfo> markers = programRepository.findMakersByRegionCode(regionType.getCenterLatitude(), regionType.getCenterLongitude(), DEFAULT_MAX_MARKERS, memberId);

        return new ProgramMarkerResponse(
                markers,
                markers.size(),
                false
        );
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
