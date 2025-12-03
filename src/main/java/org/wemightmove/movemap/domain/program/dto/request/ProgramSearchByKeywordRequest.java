package org.wemightmove.movemap.domain.program.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProgramSearchByKeywordRequest(
        @NotBlank(message = "검색어를 입력해주세요")
        @Size(min = 1, max = 100, message = "검색어는 1자 이상 100자 이하여야 합니다")
        String keyword,

        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        Long cursor
) {
        public ProgramSearchByKeywordRequest {
                if (size == null) {
                        size = 20;
                }
                if(cursor != null && cursor <= 0) {
                        cursor = null;
                }
        }

        // 공백 제거한 정규화된 키워드 반환
        public String normalizedKeyword() {
                return keyword.replaceAll("\\s+", "");
        }
}
