package org.wemightmove.movemap.domain.record.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.record.entity.CheckInRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {

    Optional<CheckInRecord> findByMemberAndCheckOutAtIsNull(Member member);

    @Query("""
                SELECT cir
                FROM CheckInRecord cir
                JOIN FETCH cir.facility f
                WHERE cir.member = :member
                  AND cir.date = :date
                  AND cir.checkOutAt IS NOT NULL
            """)
    List<CheckInRecord> findDailyCheckInRecords(@Param("member") Member member,
                                                @Param("date") LocalDate date);
}
