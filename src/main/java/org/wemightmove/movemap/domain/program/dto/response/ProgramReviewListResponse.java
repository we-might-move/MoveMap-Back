package org.wemightmove.movemap.domain.program.dto.response;

import java.util.List;

public record ProgramReviewListResponse(
        List<ProgramReviewResponse> reviews,
        Long nextCursor,
        boolean hasNext,
        int size
) {
    public static ProgramReviewListResponse of(List<ProgramReviewResponse> reviews, Long nextCursor, boolean hasNext) {
        return new ProgramReviewListResponse(reviews, nextCursor, hasNext, reviews.size());
    }
}
