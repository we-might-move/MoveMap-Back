package org.wemightmove.movemap.domain.program.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.MemberProgram;
import org.wemightmove.movemap.domain.member.repository.MemberProgramRepository;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.program.entity.Program;
import org.wemightmove.movemap.domain.program.repository.ProgramRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ProgramCommandServiceImpl implements ProgramCommandService {

    private final MemberRepository memberRepository;
    private final ProgramRepository programRepository;
    private final MemberProgramRepository memberProgramRepository;

    @Override
    @Transactional
    public void addBookmarkProgram(Long memberId, Long programId) {
        Member member = getMember(memberId);
        Program program = getProgram(programId);

        if (memberProgramRepository.existsMemberProgramByMemberAndProgram(member, program)) {
            throw new CustomException(ErrorCode.ALREADY_ADDED_BOOKMARK);
        }

        memberProgramRepository.save(MemberProgram.from(member, program));
    }

    @Override
    @Transactional
    public void deleteBookmarkProgram(Long memberId, Long programId) {
        Member member = getMember(memberId);
        Program program = getProgram(programId);

        if (!memberProgramRepository.existsMemberProgramByMemberAndProgram(member, program)) {
            throw new CustomException(ErrorCode.ALREADY_DELETED_BOOKMARK);
        }

        memberProgramRepository.deleteByMemberAndProgram(member, program);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Program getProgram(Long facilityId) {
        return programRepository.findById(facilityId).orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
