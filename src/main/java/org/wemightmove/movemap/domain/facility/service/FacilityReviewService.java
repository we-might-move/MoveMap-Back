package org.wemightmove.movemap.domain.facility.service;

import org.wemightmove.movemap.domain.facility.dto.request.FacilityReviewListRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityReviewRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityReviewListResponse;

public interface FacilityReviewService {
    void saveFacilityReview(Long memberId, Long facilityId, FacilityReviewRequest request);
    FacilityReviewListResponse getReviewList(Long memberId, FacilityReviewListRequest request);
}
