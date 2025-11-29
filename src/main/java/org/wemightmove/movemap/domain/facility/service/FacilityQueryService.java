package org.wemightmove.movemap.domain.facility.service;

import org.wemightmove.movemap.domain.facility.dto.request.FacilityMarkerRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.util.List;

public interface FacilityQueryService {
    FacilityMarkerResponse getMarkers(Long memberId);
    FacilityMarkerResponse searchMarkers(Long memberId, FacilityMarkerRequest request, List<FacilityType> facilityTypes);
}
