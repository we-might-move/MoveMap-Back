package org.wemightmove.movemap.domain.member.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record FavoriteProgramRequest(
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
        BigDecimal currentLatitude,

        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
        BigDecimal currentLongitude,

        Long cursor,  // 페이지네이션 커서(이전 응답에서 마지막 memberProgramId)

        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size
) {
    // Default 값 제공을 위한 생성자
    public FavoriteProgramRequest {
        if (size == null) {
            size = 20;  // 기본 페이지 크기
        }
    }
}
