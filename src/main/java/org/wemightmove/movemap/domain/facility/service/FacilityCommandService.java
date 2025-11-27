package org.wemightmove.movemap.domain.facility.service;

public interface FacilityCommandService {
    void addBookmarkFacility(Long memberId, Long facilityId);
    void deleteBookmarkFacility(Long memberId, Long facilityId);
}
