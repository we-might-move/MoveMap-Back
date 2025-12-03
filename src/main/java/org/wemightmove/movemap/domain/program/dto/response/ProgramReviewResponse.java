package org.wemightmove.movemap.domain.program.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.wemightmove.movemap.domain.program.dto.ProgramReviewProjection;
import org.wemightmove.movemap.domain.program.entity.ProgramReview;

import java.time.LocalDateTime;

public record ProgramReviewResponse(
        Long id,
        Long memberId,
        String memberNickname,
        Long programId,
        int rating,
        String title,
        String content
) {
    public static ProgramReviewResponse from(ProgramReview review) {
        return new ProgramReviewResponse(
                review.getId(),
                review.getMember().getId(),
                review.getMember().getNickname(),
                review.getProgram().getId(),
                review.getRating(),
                review.getTitle(),
                review.getContent()
        );
    }

    public static ProgramReviewResponse from(ProgramReviewProjection review) {
        return new ProgramReviewResponse(
                review.getReviewId(),
                review.getMemberId(),
                review.getMemberNickname(),
                review.getProgramId(),
                review.getRating(),
                review.getTitle(),
                review.getContent()
        );
    }
}