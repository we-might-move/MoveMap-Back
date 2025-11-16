package org.wemightmove.movemap.global.jwt;

public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
