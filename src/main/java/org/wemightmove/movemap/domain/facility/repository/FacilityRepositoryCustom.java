package org.wemightmove.movemap.domain.facility.repository;

import org.wemightmove.movemap.domain.facility.dto.request.FacilityInitialListRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilityMarkerRequest;
import org.wemightmove.movemap.domain.facility.dto.request.FacilitySearchListRequest;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityListResponse;
import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.math.BigDecimal;
import java.util.List;

public interface FacilityRepositoryCustom {
    List<FacilityMarkerResponse.MarkerInfo> findMakersByRegionCode(BigDecimal lat, BigDecimal lng, int maxResults);
    List<FacilityMarkerResponse.MarkerInfo> findMarkersByViewport(FacilityMarkerRequest request, String regionCode, List<FacilityType> facilityTypes);
    List<FacilityListResponse.FacilityInfo> findListByRegionCode(FacilityInitialListRequest request, BigDecimal lat, BigDecimal lng, String regionCode, Long memberId);
    List<FacilityListResponse.FacilityInfo> findListByViewport(FacilitySearchListRequest request, String regionCode, List<FacilityType> facilityTypes, Long memberId);
}
