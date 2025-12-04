package org.wemightmove.movemap.domain.record.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.record.entity.SelfRecord;

import java.time.LocalDate;
import java.util.List;

public interface SelfRecordRepository extends JpaRepository<SelfRecord, Long> {
    List<SelfRecord> findByMemberAndDate(Member member, LocalDate date);
}
