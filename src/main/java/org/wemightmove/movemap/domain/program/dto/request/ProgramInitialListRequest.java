package org.wemightmove.movemap.domain.program.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "프로그램 리스트 초기 조회 요청")
public record ProgramInitialListRequest(
        @Schema(description = "커서 (마지막 조회한 프로그램 ID)", example = "100")
        Long cursor,

        @Schema(description = "페이지 크기", example = "20")
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size
) {
    public ProgramInitialListRequest {
        if (cursor != null && cursor <= 0) {
            cursor = null;
        }
        if (size == null || size <= 0) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
    }
}
