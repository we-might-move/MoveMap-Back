package org.wemightmove.movemap.domain.facility.service;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
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
import org.wemightmove.movemap.global.search.SearchEngineRouter;
import org.wemightmove.movemap.global.search.cache.SearchCacheKey;
import org.wemightmove.movemap.global.search.cache.SearchCacheVersion;
import org.wemightmove.movemap.global.search.cache.SearchResultCache;
import org.wemightmove.movemap.global.search.index.SearchDomain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityQueryServiceImpl implements FacilityQueryService {

    private final static int maxResults = 100;
    private static final String DOMAIN = SearchDomain.FACILITY.label();

    private final FacilityRepository facilityRepository;
    private final RegionTypeRepository regionTypeRepository;
    private final MemberRepository memberRepository;
    private final SearchProperties searchProperties;
    private final SearchEngineRouter searchEngineRouter;
    private final SearchResultCache searchResultCache;
    private final SearchCacheVersion searchCacheVersion;
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

        // 엔진 스위칭 + 계측 fallback(GLOBAL §3)은 SearchEngineRouter 단일 지점에 위임.
        // 그 앞에 검색 결과 cache-aside 계층을 얹는다(설계 §4.7). enabled=false 면 캐시를 완전히 우회한다.
        String engine = searchProperties.facility().engine();
        String cacheKey = SearchCacheKey.facility(searchCacheVersion.current(), engine, keyword);
        return searchResultCache.getOrLoad(cacheKey, FacilitySimpleListResponse.class,
                () -> searchEngineRouter.route(
                        engine,
                        DOMAIN,
                        keyword == null ? 0 : keyword.length(),
                        () -> esFacilitySearchAdapter.search(keyword),
                        () -> dbFacilitySearchAdapter.search(keyword)));
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
