package org.wemightmove.movemap.domain.record.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CheckInRecordAddRequest(
        @NotNull
        Long facilityId,
        LocalDateTime checkInAt
) {
}
