package org.wemightmove.movemap.domain.program.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * 시설 마커 조회 Response
 * - 최소한의 정보만 포함 (지도 표시용)
 * - 무한 스크롤 없음
 */
public record ProgramMarkerResponse(
        List<MarkerInfo> markers,
        int totalCount,
        boolean hasSearchConditions
) {
    public record MarkerInfo(
            BigDecimal latitude,
            BigDecimal longitude,
            Integer programCount,
            String representativeName,
            boolean hasBookmark
    ) {}
}