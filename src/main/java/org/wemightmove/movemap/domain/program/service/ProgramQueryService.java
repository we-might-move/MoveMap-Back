package org.wemightmove.movemap.domain.program.service;

import org.wemightmove.movemap.domain.program.dto.request.ProgramInitialListRequest;
import org.wemightmove.movemap.domain.program.dto.request.ProgramListBySearchRequest;
import org.wemightmove.movemap.domain.program.dto.request.ProgramMarkerRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramListResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.enums.WeekDayType;

import java.util.List;

public interface ProgramQueryService {
    ProgramMarkerResponse getMarkers(Long memberId);
    ProgramMarkerResponse getMarkersBySearch(ProgramMarkerRequest request, List<FacilityType> facilityTypes, List<WeekDayType> weekDayTypes, Long memberId);
    ProgramListResponse getPrograms(Long memberId, ProgramInitialListRequest request);
    ProgramListResponse getProgramsBySearch(Long memberId, ProgramListBySearchRequest request, List<FacilityType> facilityTypes, List<WeekDayType> weekDayTypes);
}
