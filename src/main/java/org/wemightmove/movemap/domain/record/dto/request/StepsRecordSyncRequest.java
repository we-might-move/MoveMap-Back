package org.wemightmove.movemap.domain.record.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record StepsRecordSyncRequest(
        @PositiveOrZero
        int count,
        @PositiveOrZero
        double distance,
        @NotNull
        @PastOrPresent
        LocalDateTime syncedAt
) {

}
