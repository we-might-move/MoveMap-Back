package org.wemightmove.movemap.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record VerifyRequest(
        @NotNull
        String code,
        @Email
        String email
) {
}
