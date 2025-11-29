package org.wemightmove.movemap.domain.facility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityReviewRequest;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.domain.facility.entity.FacilityReview;
import org.wemightmove.movemap.domain.facility.repository.FacilityRepository;
import org.wemightmove.movemap.domain.facility.repository.FacilityReviewRepository;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class FacilityReviewServiceImpl implements FacilityReviewService {

    private final FacilityReviewRepository facilityReviewRepository;
    private final MemberRepository memberRepository;
    private final FacilityRepository facilityRepository;

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

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Facility getFacility(Long facilityId) {
        return facilityRepository.findById(facilityId).orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));
    }
}
