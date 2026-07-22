package org.wemightmove.movemap.domain.program.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.program.dto.ProgramItem;
import org.wemightmove.movemap.domain.program.search.DbProgramSearchAdapter;
import org.wemightmove.movemap.domain.program.search.EsProgramSearchAdapter;
import org.wemightmove.movemap.domain.program.dto.request.ProgramInitialListRequest;
import org.wemightmove.movemap.domain.program.dto.request.ProgramListBySearchRequest;
import org.wemightmove.movemap.domain.program.dto.request.ProgramMarkerRequest;
import org.wemightmove.movemap.domain.program.dto.request.ProgramSearchByKeywordRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramDetailResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramListResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse;
import org.wemightmove.movemap.domain.program.repository.ProgramRepository;
import org.wemightmove.movemap.global.config.SearchProperties;
import org.wemightmove.movemap.global.entity.RegionType;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.enums.WeekDayType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;
import org.wemightmove.movemap.global.search.SearchMetrics;
import org.wemightmove.movemap.global.search.index.SearchDomain;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramQueryServiceImpl implements ProgramQueryService {

    private static final int DEFAULT_MAX_MARKERS = 10;
    private static final String DOMAIN = SearchDomain.PROGRAM.label();
    private static final String ENGINE_ES = "es";

    private final MemberRepository memberRepository;
    private final ProgramRepository programRepository;
    private final RegionTypeRepository regionTypeRepository;
    private final SearchProperties searchProperties;
    private final SearchMetrics searchMetrics;
    private final EsProgramSearchAdapter esProgramSearchAdapter;
    private final DbProgramSearchAdapter dbProgramSearchAdapter;

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

    @Override
    public ProgramListResponse getProgramsBySearch(Long memberId, ProgramListBySearchRequest request, List<FacilityType> facilityTypes, List<WeekDayType> weekDayTypes) {
        String regionCode = getRegionCode(request.city(), request.district());

        int fetchSize = request.size() + 1;

        List<ProgramListResponse.ProgramItem> programs = programRepository.findProgramsByViewport(
                memberId, request, regionCode, facilityTypes, WeekDayType.getWeekDayRange(weekDayTypes), fetchSize
        );

        boolean hasNext = programs.size() > request.size();
        if (hasNext) {
            programs = programs.subList(0, request.size());
        }

        // 다음 커서 계산
        Long nextCursor = hasNext && !programs.isEmpty() ? programs.get(programs.size() - 1).id() : null;

        return ProgramListResponse.of(programs, nextCursor, hasNext);
    }

    @Override
//    @Transactional(timeout = 10)
    public ProgramSimpleListResponse searchPrograms(Long memberId, ProgramSearchByKeywordRequest request) {

        // 멤버 검증은 엔진과 무관하게 보존(레거시 계약: 없으면 MEMBER_NOT_FOUND)
        getMember(memberId);

        // 엔진 스위칭 + 계측 fallback(GLOBAL §3): engine=es 면 ES 시도, 어떤 예외든 DB 로 fallback
        if (ENGINE_ES.equalsIgnoreCase(searchProperties.program().engine())) {
            try {
                return searchMetrics.esLatency(DOMAIN)
                        .record(() -> esProgramSearchAdapter.search(request));
            } catch (Exception e) {
                searchMetrics.esFallback(DOMAIN);
                log.warn("ES search fallback→DB domain={} keyword_len={} cause={}",
                        DOMAIN, request.keyword().length(), e.toString());
                return dbProgramSearchAdapter.search(request);
            }
        }

        return dbProgramSearchAdapter.search(request);
    }

    @Override
    public ProgramDetailResponse getProgramDetail(
            Long programId,
            Long memberId,
            Double userLatitude,
            Double userLongitude
    ) {
        BigDecimal userLatDecimal = userLatitude != null ? BigDecimal.valueOf(userLatitude) : null;
        BigDecimal userLngDecimal = userLongitude != null ? BigDecimal.valueOf(userLongitude) : null;

        ProgramItem item = programRepository.findProgramDetailById(
                        programId,
                        memberId,
                        userLatDecimal,
                        userLngDecimal
                )
                .map(ProgramItem::from)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        return ProgramDetailResponse.from(item);
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
