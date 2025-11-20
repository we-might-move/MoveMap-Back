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
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
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

}
