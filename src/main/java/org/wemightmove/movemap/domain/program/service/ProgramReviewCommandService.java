package org.wemightmove.movemap.domain.program.service;

import org.wemightmove.movemap.domain.program.dto.request.SaveProgramReviewRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramReviewResponse;

public interface ProgramReviewCommandService {
    ProgramReviewResponse createReview(Long programId, Long memberId, SaveProgramReviewRequest request);
}
