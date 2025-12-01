package org.wemightmove.movemap.domain.program.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.program.dto.request.SaveProgramReviewRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramReviewResponse;
import org.wemightmove.movemap.domain.program.entity.Program;
import org.wemightmove.movemap.domain.program.entity.ProgramReview;
import org.wemightmove.movemap.domain.program.repository.ProgramRepository;
import org.wemightmove.movemap.domain.program.repository.ProgramReviewRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramReviewCommandServiceImpl implements ProgramReviewCommandService {

    private final ProgramReviewRepository programReviewRepository;
    private final ProgramRepository programRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ProgramReviewResponse createReview(Long programId, Long memberId, SaveProgramReviewRequest request) {

        // 1. 프로그램 존재 여부 확인
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. 회원 존재 여부 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 3. 중복 리뷰 확인 (한 프로그램당 1개 리뷰만 작성 가능)
        if (programReviewRepository.existsByMemberIdAndProgramId(memberId, programId)) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED);
        }

        // 4. 리뷰 생성
        ProgramReview review = ProgramReview.builder()
                .member(member)
                .program(program)
                .rating(request.rating())
                .title(request.title())
                .content(request.content())
                .build();

        ProgramReview savedReview = programReviewRepository.save(review);

        return ProgramReviewResponse.from(savedReview);
    }
}
