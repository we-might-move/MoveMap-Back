package org.wemightmove.movemap.domain.facility.repository;

import org.wemightmove.movemap.domain.facility.dto.response.FacilityMarkerResponse;

import java.math.BigDecimal;
import java.util.List;

public interface FacilityRepositoryCustom {
    List<FacilityMarkerResponse.MarkerInfo> findMakersByRegionCode(BigDecimal lat, BigDecimal lng, int maxResults);
}
