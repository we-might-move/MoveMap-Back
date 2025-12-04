package org.wemightmove.movemap.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TemporaryPasswordRequest(
        @NotBlank
        @Email
        String email
) {
}
