package org.wemightmove.movemap.domain.auth.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}
