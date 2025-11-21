package org.wemightmove.movemap.domain.record.dto.request;

import java.time.LocalDateTime;

public record CheckInRecordModifyRequest(
        LocalDateTime checkOutAt
) {
}
