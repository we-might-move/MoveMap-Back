package org.wemightmove.movemap.domain.record.service;

import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordModifyRequest;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
import org.wemightmove.movemap.domain.record.dto.response.CheckInStatusResponse;

public interface RecordService {
    void addSelfRecord(SelfRecordAddRequest request);
    void syncStepsRecord(StepsRecordSyncRequest request);
    void checkIn(CheckInRecordAddRequest request);
    void checkOut(CheckInRecordModifyRequest request);
    CheckInStatusResponse findCheckInStatus();
}
