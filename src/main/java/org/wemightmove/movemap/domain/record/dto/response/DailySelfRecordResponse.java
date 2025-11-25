package org.wemightmove.movemap.domain.record.dto.response;

import lombok.Builder;
import org.wemightmove.movemap.global.enums.FacilityType;

import java.time.LocalDate;
import java.util.List;

@Builder
public record DailySelfRecordResponse(
        LocalDate date,
        List<DailySelfRecordUnit> records
) {
    public record DailySelfRecordUnit(
            FacilityType type,
            String typeName,
            int durationMinutes
    ) { }

}
