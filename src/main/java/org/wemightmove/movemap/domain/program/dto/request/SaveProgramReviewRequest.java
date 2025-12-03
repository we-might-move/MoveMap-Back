package org.wemightmove.movemap.domain.program.dto.request;

import jakarta.validation.constraints.*;

public record SaveProgramReviewRequest(
        @NotNull(message = "평점은 필수입니다")
        @Min(value = 1, message = "평점은 1 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5 이하여야 합니다")
        Integer rating,

        @NotBlank(message = "제목은 필수입니다")
        @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하여야 합니다")
        String title,

        @Size(max = 2000, message = "내용은 2000자 이하여야 합니다")
        String content
) {
}
