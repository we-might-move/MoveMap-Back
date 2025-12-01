package org.wemightmove.movemap.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.MemberProgram;
import org.wemightmove.movemap.domain.program.entity.Program;

public interface MemberProgramRepository extends JpaRepository<MemberProgram, Long>, MemberProgramRepositoryCustom {

    @Modifying
    @Query("DELETE FROM MemberProgram mp WHERE mp.member.id = :memberId")
    int deleteAllByMemberId(@Param("memberId") Long memberId);

    boolean existsMemberProgramByMemberAndProgram(Member member, Program program);

    void deleteByMemberAndProgram(Member member, Program program);
}
