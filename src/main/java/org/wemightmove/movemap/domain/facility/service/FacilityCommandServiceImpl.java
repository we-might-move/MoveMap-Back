package org.wemightmove.movemap.domain.facility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.domain.facility.repository.FacilityRepository;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.MemberFacility;
import org.wemightmove.movemap.domain.member.repository.MemberFacilityRepository;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class FacilityCommandServiceImpl implements FacilityCommandService {

    private final MemberRepository memberRepository;
    private final FacilityRepository facilityRepository;
    private final MemberFacilityRepository memberFacilityRepository;

    @Override
    @Transactional
    public void addBookmarkFacility(Long memberId, Long facilityId) {
        Member member = getMember(memberId);
        Facility facility = getFacility(facilityId);

        if (memberFacilityRepository.existsMemberFacilitiesByMemberAndFacility(member, facility)) {
            throw new CustomException(ErrorCode.ALREADY_BOOKMARK_FACILITY);
        }

        memberFacilityRepository.save(MemberFacility.from(member, facility));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Facility getFacility(Long facilityId) {
        return facilityRepository.findById(facilityId).orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));
    }
}
