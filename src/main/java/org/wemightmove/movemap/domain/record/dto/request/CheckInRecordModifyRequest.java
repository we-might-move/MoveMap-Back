package org.wemightmove.movemap.domain.record.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

public record CheckInRecordModifyRequest(
        @NotNull
        @PastOrPresent
        LocalDateTime checkOutAt
) {
}
