package org.wemightmove.movemap.domain.league.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.wemightmove.movemap.domain.league.entity.WeeklyRegionScore;
import org.wemightmove.movemap.domain.league.repository.DailyRegionScoreRepository;
import org.wemightmove.movemap.domain.league.repository.WeeklyRegionScoreRepository;
import org.wemightmove.movemap.domain.member.event.MemberScoreUpdatedEvent;
import org.wemightmove.movemap.global.entity.RegionType;
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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberScoreUpdatedEvent event) {

        RegionType region = regionTypeRepository.getReferenceById(Long.parseLong(event.regionCd()));
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