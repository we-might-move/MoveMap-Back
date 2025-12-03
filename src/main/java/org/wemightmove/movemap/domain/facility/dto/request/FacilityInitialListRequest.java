package org.wemightmove.movemap.domain.facility.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.math.BigDecimal;
import java.util.List;

/**
 * 초기 시설 리스트 조회 요청
 * - 사용자 위치 기반 (토큰에서 추출한 지역 정보 사용)
 */
public record FacilityInitialListRequest(
        @Schema(description = "커서 (마지막 조회 시설 ID)", example = "100")
        Long cursor,

        @Schema(description = "페이지 크기", example = "20")
        @Min(1) @Max(100)
        Integer size
) {
    public FacilityInitialListRequest {
        if (size == null) {
            size = 20;
        }
    }

    public boolean hasCursor() {
        return cursor != null;
    }
}