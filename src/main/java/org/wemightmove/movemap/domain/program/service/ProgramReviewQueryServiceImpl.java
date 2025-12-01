package org.wemightmove.movemap.domain.program.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.program.dto.ProgramReviewProjection;
import org.wemightmove.movemap.domain.program.dto.request.ProgramReviewRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramReviewListResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramReviewResponse;
import org.wemightmove.movemap.domain.program.repository.ProgramReviewRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramReviewQueryServiceImpl implements ProgramReviewQueryService {
    private final ProgramReviewRepository programReviewRepository;
    private final RegionTypeRepository regionTypeRepository;
    private final MemberRepository memberRepository;

    @Override
    public ProgramReviewListResponse getProgramReviews(Long memberId, ProgramReviewRequest request) {

        Member member = getMember(memberId);

        // limit + 1 조회로 hasNext 판단
        int limit = request.size() + 1;

        String regionCode = getRegionCode(request.city(), request.district());

        List<ProgramReviewProjection> projections = programReviewRepository.findProgramReviewsWithCursor(
                request.cursor(),
                regionCode,
                request.keyword(),
                limit
        );

        // hasNext 판단 및 결과 크기 조정
        boolean hasNext = projections.size() > request.size();
        List<ProgramReviewProjection> resultProjections = hasNext
                ? projections.subList(0, request.size())
                : projections;

        // DTO 변환
        List<ProgramReviewResponse> reviews = resultProjections.stream()
                .map(ProgramReviewResponse::from)
                .toList();

        // nextCursor 계산
        Long nextCursor = hasNext && !reviews.isEmpty()
                ? reviews.get(reviews.size() - 1).id()
                : null;

        return ProgramReviewListResponse.of(reviews, nextCursor, hasNext);
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
