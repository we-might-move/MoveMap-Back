package org.wemightmove.movemap.domain.program.repository;

import org.wemightmove.movemap.domain.program.dto.request.ProgramMarkerRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.math.BigDecimal;
import java.util.List;

public interface ProgramRepositoryCustom {
    List<ProgramMarkerResponse.MarkerInfo> findMakersByRegionCode(BigDecimal lat, BigDecimal lng, int maxResults, Long memberId);
    List<ProgramMarkerResponse.MarkerInfo> findMarkersByViewport(ProgramMarkerRequest request, String regionCode, List<FacilityType> facilityTypes, List<Integer> weekDayTypes, Long memberId);

}
