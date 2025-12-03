package org.wemightmove.movemap.domain.record.dto.response;

import java.time.LocalDate;
import java.util.Map;

public record WeeklyReportResponse(
        Long childId,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        Map<String, DailyAchievement> dailyAchievements
) {

    public record DailyAchievement(
            LocalDate date,
            int score
    ) {
    }
}
