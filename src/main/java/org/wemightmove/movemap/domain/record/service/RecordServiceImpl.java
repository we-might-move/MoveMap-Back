package org.wemightmove.movemap.domain.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
import org.wemightmove.movemap.domain.record.entity.SelfRecord;
import org.wemightmove.movemap.domain.record.entity.StepsRecord;
import org.wemightmove.movemap.domain.record.repository.SelfRecordRepository;
import org.wemightmove.movemap.domain.record.repository.StepsRecordRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final MemberRepository memberRepository;
    private final SelfRecordRepository selfRecordRepository;
    private final StepsRecordRepository stepsRecordRepository;

    @Override
    public void addSelfRecord(SelfRecordAddRequest request) {
        Member member = getCurrentMember();

        if(request.hours() == 0 && request.minutes() == 0) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        SelfRecord record = SelfRecord.builder()
                .member(member)
                .date(LocalDate.now())
                .exerciseType(request.exerciseType())
                .durationMinutes(request.hours() * 60 + request.minutes())
                .build();

        selfRecordRepository.save(record);
    }

    @Override
    @Transactional
    public void syncStepsRecord(StepsRecordSyncRequest request) {
        Member member = getCurrentMember();
        LocalDate today = request.syncedAt().toLocalDate();
        StepsRecord record = stepsRecordRepository.findByMemberAndDate(member, today)
                        .orElse(StepsRecord.builder().member(member).date(today).build());
        record.update(request.count(), request.distance(), request.syncedAt());
        stepsRecordRepository.save(record);
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return memberRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
