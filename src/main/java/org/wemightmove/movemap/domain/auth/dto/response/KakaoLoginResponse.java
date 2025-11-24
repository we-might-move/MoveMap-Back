package org.wemightmove.movemap.domain.auth.dto.response;

public record KakaoLoginResponse(
        String accessToken,
        String refreshToken,
        Long kakaoId,
        boolean isNewMember
) {

    public static KakaoLoginResponse of(Long kakaoId) {
        return new KakaoLoginResponse(null, null, kakaoId, true);
    }

    public static KakaoLoginResponse of(String accessToken, String refreshToken) {
        return new KakaoLoginResponse(accessToken, refreshToken, null, false);
    }
}
