package org.wemightmove.movemap.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.MemberScore;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberScoreRepository extends JpaRepository<MemberScore, Long> {
    Optional<MemberScore> findByMemberAndDate(Member member, LocalDate date);

    List<MemberScore> findByMemberAndDateBetween(Member member, LocalDate startDate, LocalDate endDate);
}
