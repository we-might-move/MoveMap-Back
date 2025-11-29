package org.wemightmove.movemap.domain.facility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityReviewListRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityReviewRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityReviewItem;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityReviewListResponse;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.domain.facility.entity.FacilityReview;
import org.wemightmove.movemap.domain.facility.repository.FacilityRepository;
import org.wemightmove.movemap.domain.facility.repository.FacilityReviewRepository;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityReviewServiceImpl implements FacilityReviewService {

    private final FacilityReviewRepository facilityReviewRepository;
    private final MemberRepository memberRepository;
    private final FacilityRepository facilityRepository;
    private final RegionTypeRepository regionTypeRepository;

    @Override
    @Transactional
    public void saveFacilityReview(Long memberId, Long facilityId, FacilityReviewRequest request) {
        Member member = getMember(memberId);
        Facility facility = getFacility(facilityId);

        if (facilityReviewRepository.existsFacilityReviewByMemberAndFacility(member, facility)) {
            throw new CustomException(ErrorCode.ALREADY_ADDED_FACILITY_REVIEW);
        }

        FacilityReview facilityReview = FacilityReview.from(member, facility, request);

        facilityReviewRepository.save(facilityReview);
    }

    /**
     * 시설 리뷰 리스트 조회
     * - 위치 기반: 가까운 시설의 리뷰부터 반환 (거리순)
     * - 지역 기반: 최신 리뷰부터 반환 (시간순)
     * - 키워드: 시설명 또는 주소에 포함된 경우 필터링
     */
    @Override
    public FacilityReviewListResponse getReviewList(Long memberId, FacilityReviewListRequest request) {

        // size + 1 개를 조회하여 hasNext 판단
        int limit = request.size() + 1;

        List<Object[]> rawResults;

        if (request.isLocationBased()) {
            // 위치 기반 조회
            rawResults = facilityReviewRepository.findReviewsByLocation(
                    request.latitude(),
                    request.longitude(),
                    request.keyword(),
                    request.cursor(),
                    limit
            );

        } else {
            String regionCode = regionTypeRepository.findRegionByName(request.district()).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_DISTRICT)).getPrefix();
            // 지역 기반 조회
            rawResults = facilityReviewRepository.findReviewsByRegion(
                    regionCode,
                    request.keyword(),
                    request.cursor(),
                    limit
            );
        }

        // DTO 변환
        List<FacilityReviewItem> reviews = rawResults.stream()
                .map(this::mapToReviewItem)
                .collect(Collectors.toList());

        return FacilityReviewListResponse.of(reviews, request.size());
    }

    /**
     * Object[] -> FacilityReviewItem 변환
     */
    private FacilityReviewItem mapToReviewItem(Object[] row) {
        return FacilityReviewItem.of(
                ((Number) row[0]).longValue(),           // facilityId
                (String) row[1],                         // facilityName
                (String) row[2],                         // facilityType
                (String) row[3],                         // address
                (BigDecimal) row[4],                     // latitude
                (BigDecimal) row[5],                     // longitude
                row[6] != null ? ((Number) row[6]).doubleValue() : null, // distance
                ((Number) row[7]).longValue(),           // reviewId
                ((Number) row[8]).intValue(),            // rating
                (String) row[9],                         // reviewContent
                (String) row[10]                         // reviewerNickname
        );
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Facility getFacility(Long facilityId) {
        return facilityRepository.findById(facilityId).orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));
    }
}
