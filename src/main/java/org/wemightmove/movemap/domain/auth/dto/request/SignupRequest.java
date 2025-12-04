package org.wemightmove.movemap.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.enums.SexType;
import org.wemightmove.movemap.global.enums.SignupType;

public record SignupRequest(
        SignupType type,
        RoleType role,
        @Email
        String email,
        String password,
        Long kakaoId,
        String nickname,
        String sido,
        String sigungu,
        SexType sexType,
        Integer age,
        Double height,
        Double weight

) {
}
