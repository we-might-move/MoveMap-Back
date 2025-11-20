package org.wemightmove.movemap.domain.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.entity.SelfRecord;
import org.wemightmove.movemap.domain.record.repository.SelfRecordRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final MemberRepository memberRepository;
    private final SelfRecordRepository selfRecordRepository;

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

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return memberRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
