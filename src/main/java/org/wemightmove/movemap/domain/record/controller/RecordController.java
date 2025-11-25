package org.wemightmove.movemap.domain.record.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordModifyRequest;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
import org.wemightmove.movemap.domain.record.dto.response.*;
import org.wemightmove.movemap.domain.record.service.RecordService;

import java.time.LocalDate;

@RestController
@Tag(name = "Record")
@RequiredArgsConstructor
@RequestMapping("/records")
public class RecordController {

    private final RecordService recordService;

    @Operation(summary = "셀프 기록 추가", description = "사용자의 셀프 기록을 추가합니다.")
    @PostMapping("/self")
    public ResponseEntity<Void> selfRecordAdd(@RequestBody @Valid SelfRecordAddRequest request) {
        recordService.addSelfRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "일별 셀프 기록 조회", description = "사용자의 일별 셀프 기록을 조회합니다.")
    @GetMapping("/self")
    public ResponseEntity<DailySelfRecordResponse> dailySelfRecordList(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(recordService.findDailySelfRecord(date));
    }

    @Operation(summary = "걷기 기록 동기화", description = "사용자의 오늘자 걸음 수 데이터를 서버에 업로드하여 최신 상태로 동기화합니다.")
    @PostMapping("/steps")
    public ResponseEntity<Void> stepsRecordSync(@RequestBody @Valid StepsRecordSyncRequest request) {
        recordService.syncStepsRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "일별 걷기 기록 조회", description = "사용자의 일별 걷기 기록을 조회합니다.")
    @GetMapping("/steps")
    public ResponseEntity<DailyStepsRecordResponse> dailyStepsRecordList(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(recordService.findDailyStepsRecord(date));
    }

    @Operation(summary = "체크인", description = "사용자의 체크인 기록을 추가합니다.")
    @PostMapping("/checkin")
    public ResponseEntity<CheckInRecordAddResponse> checkIn(@RequestBody @Valid CheckInRecordAddRequest request) {
        CheckInRecordAddResponse response = recordService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "체크아웃", description = "현재 체크인 상태인 기록을 체크아웃합니다.")
    @PatchMapping("/checkout")
    public ResponseEntity<Void> checkOut(@RequestBody @Valid CheckInRecordModifyRequest request) {
        recordService.checkOut(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "체크인 상태 조회", description = "사용자가 현재 체크인 중인지 상태를 조회합니다.")
    @GetMapping("/checkin/status")
    public ResponseEntity<CheckInStatusResponse> checkInStatus() {
        return ResponseEntity.ok(recordService.findCheckInStatus());
    }

    @Operation(summary = "일별 체크인 기록 조회", description = "사용자의 일별 체크인 기록을 조회합니다.")
    @GetMapping("/checkin")
    public ResponseEntity<DailyCheckInRecordResponse> checkInRecordAdd(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(recordService.findDailyCheckInRecord(date));
    }

}
