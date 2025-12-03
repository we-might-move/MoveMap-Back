package org.wemightmove.movemap.domain.league.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.wemightmove.movemap.domain.league.entity.DailyRegionScore;
import org.wemightmove.movemap.domain.league.event.DailyRegionScoreCompletedEvent;
import org.wemightmove.movemap.domain.league.repository.DailyRegionScoreRepository;
import org.wemightmove.movemap.domain.member.event.MemberScoreUpdatedEvent;
import org.wemightmove.movemap.domain.member.repository.MemberScoreRepository;
import org.wemightmove.movemap.global.entity.RegionType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DailyRegionScoreUpdater {

    private final MemberScoreRepository memberScoreRepository;
    private final DailyRegionScoreRepository dailyRegionScoreRepository;
    private final RegionTypeRepository regionTypeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberScoreUpdatedEvent event) {
        RegionType region = regionTypeRepository.findRegionByPrefix(event.regionCd())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_CITY));
        LocalDate date = event.date();

        int dailyScore = memberScoreRepository
                .sumTotalScoreByRegionAndDate(region.getPrefix(), date);

        DailyRegionScore daily = dailyRegionScoreRepository
                .findByRegionAndDate(region, date)
                .orElseGet(() -> new DailyRegionScore(region, date, 0));

        daily.updateScore(dailyScore);

        dailyRegionScoreRepository.save(daily);

        eventPublisher.publishEvent(DailyRegionScoreCompletedEvent
                .builder()
                .member(event.member())
                .regionCd(event.regionCd())
                .date(event.date())
                .build()
        );
    }
}