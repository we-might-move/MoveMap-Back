package org.wemightmove.movemap.domain.record.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.record.entity.SelfRecord;

public interface SelfRecordRepository extends JpaRepository<SelfRecord, Long> {
}
