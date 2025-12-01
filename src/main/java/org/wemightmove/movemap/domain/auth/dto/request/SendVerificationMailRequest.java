package org.wemightmove.movemap.domain.auth.dto.request;

import jakarta.validation.constraints.Email;

public record SendVerificationMailRequest(
        @Email
        String email
) {
}
