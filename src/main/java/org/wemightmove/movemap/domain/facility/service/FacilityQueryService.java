package org.wemightmove.movemap.domain.facility.service;

import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;

public interface FacilityQueryService {
    FacilityMarkerResponse getMarkers(Long memberId);
}
