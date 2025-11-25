package org.wemightmove.movemap.domain.record.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.record.entity.StepsRecord;

public interface StepsRecordRepository extends JpaRepository<StepsRecord, Long> {
}
