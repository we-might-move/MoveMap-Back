package org.wemightmove.movemap.domain.record.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record StepsRecordSyncRequest(
        @PositiveOrZero
        int count,
        @PositiveOrZero
        double distance,
        LocalDateTime syncedAt
) {

}
