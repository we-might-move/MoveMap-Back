package org.wemightmove.movemap.domain.facility.repository;

import org.wemightmove.movemap.domain.facility.dto.request.FacilityMarkerRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.math.BigDecimal;
import java.util.List;

public interface FacilityRepositoryCustom {
    List<FacilityMarkerResponse.MarkerInfo> findMakersByRegionCode(BigDecimal lat, BigDecimal lng, int maxResults);
    List<FacilityMarkerResponse.MarkerInfo> findMarkersByViewport(FacilityMarkerRequest request, String regionCode, List<FacilityType> facilityTypes);
}
