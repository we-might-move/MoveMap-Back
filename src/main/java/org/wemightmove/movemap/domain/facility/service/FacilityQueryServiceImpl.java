package org.wemightmove.movemap.domain.facility.service;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.facility.dto.FacilityInfoProjection;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityInitialListRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityMarkerRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilitySearchListRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityListResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilitySimpleListResponse;
import org.wemightmove.movemap.domain.facility.repository.FacilityRepository;
import org.wemightmove.movemap.domain.facility.search.DbFacilitySearchAdapter;
import org.wemightmove.movemap.domain.facility.search.EsFacilitySearchAdapter;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.global.config.SearchProperties;
import org.wemightmove.movemap.global.entity.RegionType;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;
import org.wemightmove.movemap.global.search.SearchMetrics;
import org.wemightmove.movemap.global.search.index.SearchDomain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityQueryServiceImpl implements FacilityQueryService {

    private final static int maxResults = 100;
    private static final String DOMAIN = SearchDomain.FACILITY.label();
    private static final String ENGINE_ES = "es";

    private final FacilityRepository facilityRepository;
    private final RegionTypeRepository regionTypeRepository;
    private final MemberRepository memberRepository;
    private final SearchProperties searchProperties;
    private final SearchMetrics searchMetrics;
    private final EsFacilitySearchAdapter esFacilitySearchAdapter;
    private final DbFacilitySearchAdapter dbFacilitySearchAdapter;

    @Override
    public FacilityMarkerResponse getMarkers(Long memberId) {

        Member member = getMember(memberId);

        RegionType regionType = regionTypeRepository.findChildRegionTypeByPrefix(member.getRegionCode()).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_DISTRICT));

        List<FacilityMarkerResponse.MarkerInfo> markers = facilityRepository.findMakersByRegionCode(regionType.getCenterLatitude(), regionType.getCenterLongitude(), maxResults);

        return new FacilityMarkerResponse(
                markers,
                markers.size(),
                false
        );
    }

    /**
     * Viewport + 검색 조건으로 마커 조회
     */
    public FacilityMarkerResponse searchMarkers(Long memberId, FacilityMarkerRequest request, List<FacilityType> facilityTypes) {

        Member member = getMember(memberId);

        String regionCode = getRegionCode(request.city(), request.district());

        List<FacilityMarkerResponse.MarkerInfo> markers = facilityRepository.findMarkersByViewport(request, regionCode, facilityTypes);

        return new FacilityMarkerResponse(
                markers,
                markers.size(),
                request.hasSearchConditions()
        );
    }

    @Override
    public FacilityListResponse getFacilityList(Long memberId, FacilityInitialListRequest request) {
        /**
         * FIXME : member 가 active 상태인지도 체크하기
         */
        Member member = getMember(memberId);

        RegionType regionType = regionTypeRepository.findChildRegionTypeByPrefix(member.getRegionCode()).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_DISTRICT));

        List<FacilityListResponse.FacilityInfo> facilities = facilityRepository.findListByRegionCode(
                request, regionType.getCenterLatitude(), regionType.getCenterLongitude(), member.getRegionCode(), memberId
        );

        return buildPagedResponse(facilities, request.size());
    }

    @Override
    public FacilityListResponse searchFacilityList(Long memberId, FacilitySearchListRequest request, List<FacilityType> facilityTypes) {
        /**
         * FIXME : member 가 active 상태인지도 체크하기
         */
        Member member = getMember(memberId);

        String regionCode = getRegionCode(request.city(), request.district());

        List<FacilityListResponse.FacilityInfo> facilities = facilityRepository.findListByViewport(request, regionCode, facilityTypes, memberId);

        return buildPagedResponse(facilities, request.size());
    }

    @Override
    public FacilitySimpleListResponse searchFacilityListByKeyword(Long memberId, String keyword) {
        // 멤버 검증은 엔진과 무관하게 보존(레거시 계약: 없으면 MEMBER_NOT_FOUND)
        getMember(memberId);

        // 엔진 스위칭 + 계측 fallback(GLOBAL §3): engine=es 면 ES 시도, 어떤 예외든 DB 로 fallback
        if (ENGINE_ES.equalsIgnoreCase(searchProperties.facility().engine())) {
            try {
                return searchMetrics.esLatency(DOMAIN)
                        .record(() -> esFacilitySearchAdapter.search(keyword));
            } catch (Exception e) {
                searchMetrics.esFallback(DOMAIN);
                log.warn("ES search fallback→DB domain={} keyword_len={} cause={}",
                        DOMAIN, keyword == null ? 0 : keyword.length(), e.toString());
                return dbFacilitySearchAdapter.search(keyword);
            }
        }

        return dbFacilitySearchAdapter.search(keyword);
    }

    @Override
    public FacilityListResponse.FacilityInfo getFacilityInfo(Long memberId, Long facilityId, BigDecimal lat, BigDecimal lng) {
        Member member = getMember(memberId);

        FacilityInfoProjection projection = facilityRepository.findFacilityInfoWithLocation(
                facilityId, memberId, lat, lng
        ).orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));

        return new FacilityListResponse.FacilityInfo(
                projection.getId(),
                projection.getName(),
                projection.getLatitude(),
                projection.getLongitude(),
                projection.getFacilityType(),
                projection.getFacilitySubtype(),
                projection.getAddress(),
                projection.getIsVoucherAvailable(),
                projection.getDistanceMeters(),
                projection.getAvgRating(),
                projection.getReviewCount(),
                projection.getIsBookmarked()
        );
    }

    /**
     * 페이징 응답 생성
     * - size + 1개를 조회하여 hasNext 판단
     * - 실제 반환은 size 개만
     */
    private FacilityListResponse buildPagedResponse(List<FacilityListResponse.FacilityInfo> facilities, int size) {
        boolean hasNext = facilities.size() > size;

        // 실제 반환할 리스트 (size 개만)
        List<FacilityListResponse.FacilityInfo> content = hasNext
                ? facilities.subList(0, size)
                : facilities;

        // 다음 커서 (마지막 항목의 ID)
        Long nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).id()
                : null;

        return FacilityListResponse.of(content, nextCursor, hasNext);
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
