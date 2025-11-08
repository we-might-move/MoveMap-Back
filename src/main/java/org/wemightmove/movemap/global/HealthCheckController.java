package org.wemightmove.movemap.global;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health-Check", description = "서버가 정상적으로 실행 중인지 서버 상태를 확인합니다.")
@RequestMapping("/health")
public class HealthCheckController {

    @Operation(summary = "헬스체크", description = "서버 프로세스가 살아있다면 200 OK를 반환합니다.")
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}