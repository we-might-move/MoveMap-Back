package org.wemightmove.movemap.domain.facility.service;

import org.wemightmove.movemap.domain.facility.dto.request.FacilityReviewRequest;

public interface FacilityCommandService {
    void addBookmarkFacility(Long memberId, Long facilityId);
    void deleteBookmarkFacility(Long memberId, Long facilityId);
    void saveFacilityReview(Long memberId, Long facilityId, FacilityReviewRequest request);
}
