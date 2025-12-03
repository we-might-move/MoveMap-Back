package org.wemightmove.movemap.domain.league.listener;


import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.league.entity.WeeklyRegionScore;
import org.wemightmove.movemap.domain.league.event.DailyRegionScoreCompletedEvent;
import org.wemightmove.movemap.domain.league.repository.DailyRegionScoreRepository;
import org.wemightmove.movemap.domain.league.repository.WeeklyRegionScoreRepository;
import org.wemightmove.movemap.global.entity.RegionType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class WeeklyRegionScoreUpdater {

    private final DailyRegionScoreRepository dailyRegionScoreRepository;
    private final WeeklyRegionScoreRepository weeklyRegionScoreRepository;
    private final RegionTypeRepository regionTypeRepository;

    @Async
    @Transactional
    @EventListener
    public void handle(DailyRegionScoreCompletedEvent event) {
        RegionType region = regionTypeRepository.findRegionByPrefix(event.regionCd())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_CITY));
        LocalDate date = event.date();

        WeekFields weekFields = WeekFields.of(Locale.KOREA);
        int year = date.getYear();
        int month = date.getMonthValue();
        int weekNumber = date.get(WeekFields.of(DayOfWeek.MONDAY, 4).weekOfMonth());

        // 해당 주의 시작(월요일)과 끝(일요일) 계산
        LocalDate startOfWeek = date.with(WeekFields.of(DayOfWeek.MONDAY, 4).dayOfWeek(), 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // 1. 해당 주간의 점수 합산 (DailyRegionScore 합)
        int weeklyScore = dailyRegionScoreRepository
                .sumScoreByRegionAndDateBetween(region, startOfWeek, endOfWeek);

        // 2. WeeklyRegionScore upsert
        WeeklyRegionScore weekly = weeklyRegionScoreRepository
                .findByRegionAndYearAndWeekNumber(region, year, weekNumber)
                .orElseGet(() -> new WeeklyRegionScore(region, year, month, weekNumber, 0));

        weekly.updateScore(weeklyScore);

        weeklyRegionScoreRepository.save(weekly);
    }
}