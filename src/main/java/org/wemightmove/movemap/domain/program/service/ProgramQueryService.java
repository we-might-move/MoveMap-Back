package org.wemightmove.movemap.domain.program.service;

import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;

public interface ProgramQueryService {
    ProgramMarkerResponse getMarkers(Long memberId);
}
