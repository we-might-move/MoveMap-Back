package org.wemightmove.movemap.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueTokenRequest(
        @NotBlank
        String refreshToken
) {
}
