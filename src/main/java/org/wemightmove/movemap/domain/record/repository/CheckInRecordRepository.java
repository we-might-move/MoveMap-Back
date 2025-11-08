package org.wemightmove.movemap.domain.record.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.record.entity.CheckInRecord;

public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {
}
