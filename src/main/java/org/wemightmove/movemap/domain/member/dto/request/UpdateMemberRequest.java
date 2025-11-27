package org.wemightmove.movemap.domain.member.dto.request;

import jakarta.validation.constraints.*;
import org.wemightmove.movemap.global.enums.SexType;

public record UpdateMemberRequest(
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9_-]+$", message = "닉네임은 한글, 영문, 숫자, _, - 만 사용 가능합니다")
        String nickname,

        @Size(max = 100, message = "학교명은 100자 이하여야 합니다")
        String school,

        @Size(min = 2, max = 10, message = "시/도는 2자 이상 10자 이하여야 합니다")
        String city,

        @Size(min = 2, max = 10, message = "구/군은 2자 이상 10자 이하여야 합니다")
        String district,

        @Pattern(
                regexp = "^(WOMAN|MAN)$",
                message = "성별은 WOMAN, MAN 중 하나여야 합니다"
        )
        SexType sex,

        @Min(value = 1, message = "나이는 1 이상이어야 합니다")
        @Max(value = 150, message = "나이는 150 이하여야 합니다")
        Integer age,

        @DecimalMin(value = "0.0", message = "키는 0 이상이어야 합니다")
        @DecimalMax(value = "300.0", message = "키는 300 이하여야 합니다")
        Double height,

        @DecimalMin(value = "0.0", message = "몸무게는 0 이상이어야 합니다")
        @DecimalMax(value = "500.0", message = "몸무게는 500 이하여야 합니다")
        Double weight
) {

        public boolean isValidRegionUpdate() {
                return (city == null && district == null) || (city != null && district != null);
        }
        public boolean hasAnyFieldToUpdate() {
                return nickname != null ||
                        school != null ||
                        city != null ||
                        district != null ||
                        sex != null ||
                        age != null ||
                        height != null ||
                        weight != null;
        }
}
