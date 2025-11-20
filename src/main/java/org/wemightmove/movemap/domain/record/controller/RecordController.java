package org.wemightmove.movemap.domain.record.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
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
    public ResponseEntity<Void> checkIn(@RequestBody CheckInRecordAddRequest request) {
        recordService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
