package org.wemightmove.movemap.domain.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.facility.entity.Facility;
import org.wemightmove.movemap.domain.facility.repository.FacilityRepository;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.MemberScore;
import org.wemightmove.movemap.domain.member.entity.ParentChild;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.member.repository.MemberScoreRepository;
import org.wemightmove.movemap.domain.member.repository.ParentChildRepository;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordModifyRequest;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
import org.wemightmove.movemap.domain.record.dto.response.*;
import org.wemightmove.movemap.domain.record.entity.CheckInRecord;
import org.wemightmove.movemap.domain.record.entity.SelfRecord;
import org.wemightmove.movemap.domain.record.entity.StepsRecord;
import org.wemightmove.movemap.domain.record.repository.CheckInRecordRepository;
import org.wemightmove.movemap.domain.record.repository.SelfRecordRepository;
import org.wemightmove.movemap.domain.record.repository.StepsRecordRepository;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final MemberRepository memberRepository;
    private final SelfRecordRepository selfRecordRepository;
    private final StepsRecordRepository stepsRecordRepository;
    private final CheckInRecordRepository checkInRecordRepository;
    private final FacilityRepository facilityRepository;
    private final MemberScoreRepository memberScoreRepository;
    private final ParentChildRepository parentChildRepository;

    @Override
    @Transactional
    public void addSelfRecord(SelfRecordAddRequest request) {
        Member member = getCurrentMember();

        if (request.hours() == 0 && request.minutes() == 0) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        int durationMinutes = request.hours() * 60 + request.minutes();

        SelfRecord record = SelfRecord.builder()
                .member(member)
                .date(LocalDate.now())
                .exerciseType(request.exerciseType())
                .durationMinutes(durationMinutes)
                .build();

        selfRecordRepository.save(record);

        //점수 계산을 위한 필드 업데이트
        MemberScore score = getOrCreateMemberScore(member, LocalDate.now());
        score.addSelfDuration(durationMinutes);
    }

    @Override
    @Transactional(readOnly = true)
    public DailySelfRecordResponse findDailySelfRecord(LocalDate date) {
        Member member = getCurrentMember();

        // 부모가 아니면 자기 기록을 열람하는 것이므로 할당
        Member target = member;

        // 만약 부모이면 연결된 자식 기록 열람하도록 함
        if (member.getRole().equals(RoleType.PARENT)) {
            List<ParentChild> childList = parentChildRepository.findAllByParent(member);

            // 자식이 아예 없으면 없다고 설정
            if (childList.isEmpty()) {
                throw new CustomException(ErrorCode.CHILD_NOT_FOUND);
            }

            // 자식이 있으면 해당 자식을 target 으로 설정
            target = childList.get(0).getChild();
        }

        List<SelfRecord> records = selfRecordRepository.findByMemberAndDate(target, date);
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
        LocalDate date = request.syncedAt().toLocalDate();
        StepsRecord record = stepsRecordRepository.findByMemberAndDate(member, date)
                .orElse(StepsRecord.builder().member(member).date(date).build());
        record.update(request.count(), request.distance(), request.syncedAt());
        stepsRecordRepository.save(record);

        //점수 계산을 위한 필드 업데이트
        MemberScore score = getOrCreateMemberScore(member, date);
        score.updateTotalSteps(request.count());
    }

    @Override
    @Transactional(readOnly = true)
    public DailyStepsRecordResponse findDailyStepsRecord(LocalDate date) {
        Member member = getCurrentMember();

        // 부모가 아니면 자기 기록을 열람하는 것이므로 할당
        Member target = member;

        // 만약 부모이면 연결된 자식 기록 열람하도록 함
        if (member.getRole().equals(RoleType.PARENT)) {
            List<ParentChild> childList = parentChildRepository.findAllByParent(member);

            // 자식이 아예 없으면 없다고 설정
            if (childList.isEmpty()) {
                throw new CustomException(ErrorCode.CHILD_NOT_FOUND);
            }

            // 자식이 있으면 해당 자식을 target 으로 설정
            target = childList.get(0).getChild();
        }

        StepsRecord record = stepsRecordRepository.findByMemberAndDate(target, date)
                .orElse(StepsRecord.builder().member(target).date(date).build());
        DailyStepsRecordResponse response = DailyStepsRecordResponse.builder()
                .date(date)
                .count(record.getCount())
                .distance(record.getDistance())
                .build();
        return response;
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
    @Transactional
    public void checkOut(CheckInRecordModifyRequest request) {
        Member member = getCurrentMember();
        CheckInRecord record = checkInRecordRepository.findById(request.id())
                .orElseThrow(() -> {
                    throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
                });
        if (!record.getMember().equals(member)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (record.getCheckOutAt() != null) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED);
        }
        record.checkout(request.checkOutAt());
        checkInRecordRepository.save(record);

        //점수 계산을 위한 필드 업데이트
        MemberScore score = getOrCreateMemberScore(member, record.getDate());
        score.addCheckInDuration(record.getDurationMinutes());
    }


    @Override
    @Transactional(readOnly = true)
    public CheckInStatusResponse findCheckInStatus() {
        Member member = getCurrentMember();
        boolean isCheckedIn = checkInRecordRepository
                .findByMemberAndCheckOutAtIsNull(member)
                .isPresent();
        return new CheckInStatusResponse(isCheckedIn);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyCheckInRecordResponse findDailyCheckInRecord(LocalDate date) {
        Member member = getCurrentMember();

        // 부모가 아니면 자기 기록을 열람하는 것이므로 할당
        Member target = member;

        // 만약 부모이면 연결된 자식 기록 열람하도록 함
        if (member.getRole().equals(RoleType.PARENT)) {
            List<ParentChild> childList = parentChildRepository.findAllByParent(member);

            // 자식이 아예 없으면 없다고 설정
            if (childList.isEmpty()) {
                throw new CustomException(ErrorCode.CHILD_NOT_FOUND);
            }

            // 자식이 있으면 해당 자식을 target 으로 설정
            target = childList.get(0).getChild();
        }

        List<CheckInRecord> records = checkInRecordRepository.findDailyCheckInRecords(target, date);
        DailyCheckInRecordResponse response = DailyCheckInRecordResponse.builder()
                .date(date)
                .records(records.stream()
                        .map(record -> DailyCheckInRecordResponse.DailyCheckInRecordUnit.builder()
                                .facilityId(record.getFacility().getId())
                                .facilityName(record.getFacility().getName())
                                .checkInAt(record.getCheckInAt())
                                .checkOutAt(record.getCheckOutAt())
                                .durationMinutes(record.getDurationMinutes())
                                .build()
                        ).toList())
                .build();
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MonthDailyFlagsResponse findMonthDailyFlagsList(int year, int month) {
        Member member = getCurrentMember();

        // 부모가 아니면 자기 기록을 열람하는 것이므로 할당
        Member target = member;

        // 만약 부모이면 연결된 자식 기록 열람하도록 함
        if (member.getRole().equals(RoleType.PARENT)) {
            List<ParentChild> childList = parentChildRepository.findAllByParent(member);

            // 자식이 아예 없으면 없다고 설정
            if (childList.isEmpty()) {
                throw new CustomException(ErrorCode.CHILD_NOT_FOUND);
            }

            // 자식이 있으면 해당 자식을 target 으로 설정
            target = childList.get(0).getChild();
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<MemberScore> scores =
                memberScoreRepository.findByMemberAndDateBetween(target, startDate, endDate);

        int daysInMonth = startDate.lengthOfMonth();
        boolean[] flags = new boolean[daysInMonth + 1];

        for (MemberScore s : scores) {
            int day = s.getDate().getDayOfMonth();

            boolean flag = s.getTotalSelfDuration() > 0
                    || s.getTotalCheckinDuration() > 0
                    || s.getTotalSteps() >= 10000;

            flags[day] = flag;
        }

        Map<Integer, Boolean> flagMap = new HashMap<>();
        for (int day = 1; day <= daysInMonth; day++) {
            flagMap.put(day, flags[day]);
        }

        return MonthDailyFlagsResponse.builder()
                .year(year)
                .month(month)
                .flags(flagMap)
                .build();
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return memberRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private MemberScore getOrCreateMemberScore(Member member, LocalDate date) {
        return memberScoreRepository.findByMemberAndDate(member, date)
                .orElseGet(() -> memberScoreRepository.save(new MemberScore(member, date)));
    }
}
