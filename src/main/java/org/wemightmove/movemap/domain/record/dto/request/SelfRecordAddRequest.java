package org.wemightmove.movemap.domain.record.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import org.wemightmove.movemap.global.enums.FacilityType;

public record SelfRecordAddRequest(
        FacilityType exerciseType,
        @PositiveOrZero
        int hours,
        @PositiveOrZero
        int minutes
) {
}
