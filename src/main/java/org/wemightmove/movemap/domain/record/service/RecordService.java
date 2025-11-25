package org.wemightmove.movemap.domain.record.service;

import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordModifyRequest;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
import org.wemightmove.movemap.domain.record.dto.response.*;

import java.time.LocalDate;

public interface RecordService {
    void addSelfRecord(SelfRecordAddRequest request);
    DailySelfRecordResponse findDailySelfRecord(LocalDate date);
    void syncStepsRecord(StepsRecordSyncRequest request);
    DailyStepsRecordResponse findDailyStepsRecord(LocalDate date);
    CheckInRecordAddResponse checkIn(CheckInRecordAddRequest request);
    void checkOut(CheckInRecordModifyRequest request);
    CheckInStatusResponse findCheckInStatus();
    DailyCheckInRecordResponse findDailyCheckInRecord(LocalDate date);
}
