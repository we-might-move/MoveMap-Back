package org.wemightmove.movemap.domain.record.service;

import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;

public interface RecordService {
    void addSelfRecord(SelfRecordAddRequest request);
    void syncStepsRecord(StepsRecordSyncRequest request);
}
