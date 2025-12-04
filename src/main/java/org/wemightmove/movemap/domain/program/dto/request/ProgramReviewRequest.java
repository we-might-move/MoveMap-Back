package org.wemightmove.movemap.domain.program.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProgramReviewRequest(
        String city,
        String district,

        @Size(max = 100, message = "키워드는 100자를 초과할 수 없습니다")
        String keyword,

        Long cursor,

        @Min(value = 1, message = "페이지 크기는 최소 1이어야 합니다")
        @Max(value = 50, message = "페이지 크기는 최대 50이어야 합니다")
        Integer size
) {
    public ProgramReviewRequest {
        // 기본값 설정
        if (size == null || size < 1) {
            size = 20;
        }
        if (size > 50) {
            size = 50;
        }

        if (cursor != null && cursor <= 0) {
            cursor = null;
        }
    }
}