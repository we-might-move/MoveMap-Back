package org.wemightmove.movemap.domain.record.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.record.entity.StepsRecord;

import java.time.LocalDate;
import java.util.Optional;

public interface StepsRecordRepository extends JpaRepository<StepsRecord, Long> {
    Optional<StepsRecord> findByMemberAndDate(Member member, LocalDate date);
}
