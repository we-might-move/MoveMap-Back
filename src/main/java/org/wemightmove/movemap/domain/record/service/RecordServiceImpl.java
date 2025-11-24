package org.wemightmove.movemap.domain.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.domain.facility.repository.FacilityRepository;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordModifyRequest;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
import org.wemightmove.movemap.domain.record.dto.response.CheckInRecordAddResponse;
import org.wemightmove.movemap.domain.record.dto.response.CheckInStatusResponse;
import org.wemightmove.movemap.domain.record.dto.response.DailySelfRecordResponse;
import org.wemightmove.movemap.domain.record.entity.CheckInRecord;
import org.wemightmove.movemap.domain.record.entity.SelfRecord;
import org.wemightmove.movemap.domain.record.entity.StepsRecord;
import org.wemightmove.movemap.domain.record.repository.CheckInRecordRepository;
import org.wemightmove.movemap.domain.record.repository.SelfRecordRepository;
import org.wemightmove.movemap.domain.record.repository.StepsRecordRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final MemberRepository memberRepository;
    private final SelfRecordRepository selfRecordRepository;
    private final StepsRecordRepository stepsRecordRepository;
    private final CheckInRecordRepository checkInRecordRepository;
    private final FacilityRepository facilityRepository;

    @Override
    public void addSelfRecord(SelfRecordAddRequest request) {
        Member member = getCurrentMember();

        if (request.hours() == 0 && request.minutes() == 0) {
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
    public DailySelfRecordResponse findDailySelfRecord(LocalDate date) {
        Member member = getCurrentMember();
        List<SelfRecord> records = selfRecordRepository.findByMemberAndDate(member, date);
        DailySelfRecordResponse response = DailySelfRecordResponse.builder()
                .date(date)
                .records(records.stream()
                        .map(record -> new DailySelfRecordResponse.DailySelfRecordUnit(record.getExerciseType(), record.getExerciseType().getName(), record.getDurationMinutes()))
                        .toList())
                .build();
        return response;
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

    @Override
    public CheckInRecordAddResponse checkIn(CheckInRecordAddRequest request) {
        Member member = getCurrentMember();

        //이미 체크인 상태인지 검사 → 중복 체크인 방지
        checkInRecordRepository.findByMemberAndCheckOutAtIsNull(member)
                .ifPresent(record -> {
                    throw new CustomException(ErrorCode.BAD_REQUEST);
                });

        Facility facility = facilityRepository.findById(request.facilityId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        CheckInRecord record = CheckInRecord.builder()
                .member(member)
                .facility(facility)
                .checkInAt(request.checkInAt())
                .build();

        record = checkInRecordRepository.save(record);
        return new CheckInRecordAddResponse(record.getId());
    }

    @Override
    public void checkOut(CheckInRecordModifyRequest request) {
        Member member = getCurrentMember();
        CheckInRecord record = checkInRecordRepository.findById(request.id())
                .orElseThrow(() -> {
                    throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
                });
        if(!record.getMember().equals(member)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if(record.getCheckOutAt() != null) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED);
        }
        record.checkout(request.checkOutAt());
        checkInRecordRepository.save(record);
    }

    @Override
    public CheckInStatusResponse findCheckInStatus() {
        Member member = getCurrentMember();
        boolean isCheckedIn = checkInRecordRepository
                .findByMemberAndCheckOutAtIsNull(member)
                .isPresent();
        return new CheckInStatusResponse(isCheckedIn);
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return memberRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
