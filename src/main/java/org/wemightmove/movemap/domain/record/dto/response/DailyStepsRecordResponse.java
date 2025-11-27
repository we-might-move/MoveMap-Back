package org.wemightmove.movemap.domain.record.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DailyStepsRecordResponse(
        LocalDate date,
        int count,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
        double distance
) {
}
