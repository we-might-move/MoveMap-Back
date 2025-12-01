package org.wemightmove.movemap.domain.program.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.program.dto.request.ProgramInitialListRequest;
import org.wemightmove.movemap.domain.program.dto.request.ProgramMarkerRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramListResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;
import org.wemightmove.movemap.domain.program.repository.ProgramRepository;
import org.wemightmove.movemap.global.entity.RegionType;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.enums.WeekDayType;
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

        int totalPrograms = markers.stream()
                .mapToInt(ProgramMarkerResponse.MarkerInfo::programCount)
                .sum();

        return new ProgramMarkerResponse(
                markers,
                totalPrograms,
                false
        );
    }

    @Override
    public ProgramMarkerResponse getMarkersBySearch(ProgramMarkerRequest request, List<FacilityType> facilityTypes, List<WeekDayType> weekDayTypes, Long memberId) {
        // 지역 코드 조회 (city + district 기반)
        String regionCode = getRegionCode(request.city(), request.district());

        List<ProgramMarkerResponse.MarkerInfo> markers =
                programRepository.findMarkersByViewport(
                        request, regionCode, facilityTypes, WeekDayType.getWeekDayRange(weekDayTypes), memberId
                );

        int totalPrograms = markers.stream()
                .mapToInt(ProgramMarkerResponse.MarkerInfo::programCount)
                .sum();

        return new ProgramMarkerResponse(
                markers,
                totalPrograms,
                request.hasSearchConditions()
        );
    }

    @Override
    public ProgramListResponse getPrograms(Long memberId, ProgramInitialListRequest request) {
        Member member = getMember(memberId);

        int fetchSize = request.size() + 1;
        List<ProgramListResponse.ProgramItem> programs = programRepository.findProgramsByRegion(
                memberId, member.getRegionCode(), request.cursor(), fetchSize
        );

        // 다음 페이지 존재 여부 확인
        boolean hasNext = programs.size() > request.size();
        if(hasNext) {
            programs = programs.subList(0, request.size());
        }

        // 다음 커서 계산
        Long nextCursor = hasNext && !programs.isEmpty() ? programs.get(programs.size() - 1).id() : null;

        return ProgramListResponse.of(programs, nextCursor, hasNext);
    }

    private String getRegionCode(String city, String district) {
        if (city == null && district == null) return null;
        else if(city == null) return regionTypeRepository.findRegionByName(district).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_DISTRICT)).getPrefix();
        else if(district == null) return regionTypeRepository.findRegionByName(city).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_CITY)).getPrefix();

        return regionTypeRepository.findRegionByNameAndParentName(district, city).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_FAIR)).getPrefix();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
