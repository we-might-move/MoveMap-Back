package org.wemightmove.movemap.domain.record.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DailyCheckInRecordResponse(
        LocalDate date,
        List<DailyCheckInRecordUnit> records

) {
    @Builder
    public record DailyCheckInRecordUnit(
            long facilityId,
            String facilityName,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalDateTime checkInAt,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalDateTime checkOutAt,
            int durationMinutes
    ) { }
}
