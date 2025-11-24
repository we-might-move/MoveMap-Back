package org.wemightmove.movemap.domain.record.service;

import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordModifyRequest;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
import org.wemightmove.movemap.domain.record.dto.response.CheckInRecordAddResponse;
import org.wemightmove.movemap.domain.record.dto.response.CheckInStatusResponse;
import org.wemightmove.movemap.domain.record.dto.response.DailySelfRecordResponse;

import java.time.LocalDate;

public interface RecordService {
    void addSelfRecord(SelfRecordAddRequest request);
    DailySelfRecordResponse findDailySelfRecord(LocalDate date);
    void syncStepsRecord(StepsRecordSyncRequest request);
    CheckInRecordAddResponse checkIn(CheckInRecordAddRequest request);
    void checkOut(CheckInRecordModifyRequest request);
    CheckInStatusResponse findCheckInStatus();
}
