package org.wemightmove.movemap.global.search.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wemightmove.movemap.global.search.index.BulkReindexer;

import java.util.Map;

/**
 * 검색 인덱스 관리(수동 전량 재색인) 엔드포인트.
 * <p>
 * {@code POST /internal/search/reindex} → 모든 도메인 전량 재색인 후 도메인별 색인 문서 수를 반환한다.
 * <p>
 * ⚠️ 이 경로는 SecurityConfig 화이트리스트에 추가하지 않았으므로 JWT 인증이 필요하다(인증된 사용자면 호출 가능).
 * 프로덕션에서는 반드시 ADMIN 역할로 제한해야 한다(예: {@code @PreAuthorize("hasRole('ADMIN')")}).
 * 현재 스코프(T4)에서는 역할 기반 제한을 도입하지 않는다.
 */
@RestController
@Tag(name = "SearchIndexAdmin")
@RequestMapping("/internal/search")
@RequiredArgsConstructor
public class SearchIndexAdminController {

    private final BulkReindexer bulkReindexer;

    @Operation(summary = "검색 인덱스 전량 재색인(관리용)",
            description = "program/facility 를 PG→ES 로 전량 재색인하고 도메인별 색인 문서 수를 반환한다. "
                    + "JWT 인증 필요. 프로덕션에서는 ADMIN 역할로 제한할 것.")
    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Long>> reindex() {
        return ResponseEntity.ok(bulkReindexer.reindexAll());
    }
}
