package org.wemightmove.movemap.domain.record.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordModifyRequest;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
import org.wemightmove.movemap.domain.record.dto.response.CheckInResponse;
import org.wemightmove.movemap.domain.record.dto.response.CheckInStatusResponse;
import org.wemightmove.movemap.domain.record.service.RecordService;

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

    @Operation(summary = "걷기 기록 동기화", description = "사용자의 오늘자 걸음 수 데이터를 서버에 업로드하여 최신 상태로 동기화합니다.")
    @PostMapping("/steps")
    public ResponseEntity<Void> stepsRecordSync(@RequestBody @Valid StepsRecordSyncRequest request) {
        recordService.syncStepsRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "체크인", description = "사용자의 체크인 기록을 추가합니다.")
    @PostMapping("/checkin")
    public ResponseEntity<CheckInResponse> checkIn(@RequestBody @Valid CheckInRecordAddRequest request) {
        CheckInResponse response = recordService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

/* <<<<<<<<<<<<<<  ✨ Windsurf Command ⭐ >>>>>>>>>>>>>>>> */
/**
 * 체크인 상태를 업데이는 메서
 * @Operation(summary = "체크인 상태를 업데이", description = "")
 * @PatchMapping("/checkout")
 * public ResponseEntity<Void> checkOut(@RequestBody CheckInRecordModifyRequest request) {
 *   recordService.checkOut(request);
 *   return ResponseEntity.ok().build();
/* <<<<<<<<<<  39190545-0e3a-4686-a078-9bb7017528e3  >>>>>>>>>>> */
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

}
