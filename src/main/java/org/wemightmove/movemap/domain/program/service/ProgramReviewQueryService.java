package org.wemightmove.movemap.domain.program.service;

import org.wemightmove.movemap.domain.program.dto.request.ProgramReviewRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramReviewListResponse;

public interface ProgramReviewQueryService {
    ProgramReviewListResponse getProgramReviews(Long memberId, ProgramReviewRequest request);
}
