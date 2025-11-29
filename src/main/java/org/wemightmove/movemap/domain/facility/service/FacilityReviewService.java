package org.wemightmove.movemap.domain.facility.service;

import org.wemightmove.movemap.domain.facility.dto.request.FacilityReviewRequest;

public interface FacilityReviewService {
    void saveFacilityReview(Long memberId, Long facilityId, FacilityReviewRequest request);
}
