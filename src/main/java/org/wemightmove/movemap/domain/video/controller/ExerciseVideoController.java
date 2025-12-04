package org.wemightmove.movemap.domain.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wemightmove.movemap.domain.video.dto.response.VideoCodeResponse;
import org.wemightmove.movemap.domain.video.service.ExerciseVideoService;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Video")
public class ExerciseVideoController {

    private final ExerciseVideoService exerciseVideoService;

    @Operation(summary = "홈화면 랜덤 운동 동영상 코드 조회", description = "운동처방 동영상 중 하나를 랜덤으로 선택하여 유튜브 영상 코드만 반환합니다.")
    @GetMapping
    public ResponseEntity<VideoCodeResponse> getRandomVideo() {
        return ResponseEntity.ok(exerciseVideoService.getRandomVideoCode());
    }
}
