package org.wemightmove.movemap.domain.auth.service;

import org.wemightmove.movemap.domain.auth.dto.request.KakaoLoginRequest;
import org.wemightmove.movemap.domain.auth.dto.request.LoginRequest;
import org.wemightmove.movemap.domain.auth.dto.request.SignupRequest;
import org.wemightmove.movemap.domain.auth.dto.response.KakaoLoginResponse;
import org.wemightmove.movemap.global.jwt.TokenDto;

public interface AuthService {
    void signup(SignupRequest request);
    TokenDto login(LoginRequest request);
    TokenDto reissue(String accessToken, String refreshToken);
    KakaoLoginResponse loginWithKakao(KakaoLoginRequest request);
}
