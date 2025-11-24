package org.wemightmove.movemap.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.auth.dto.request.LoginRequest;
import org.wemightmove.movemap.domain.auth.dto.response.KakaoLoginResponse;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.global.client.KakaoClient;
import org.wemightmove.movemap.global.client.dto.KakaoProfileResponse;
import org.wemightmove.movemap.global.client.dto.KakaoTokenResponse;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.jwt.JwtTokenProvider;
import org.wemightmove.movemap.global.jwt.TokenDto;
import org.wemightmove.movemap.global.security.CustomUserDetails;
import org.wemightmove.movemap.global.util.RedisService;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final KakaoClient kakaoClient;
    private final MemberRepository memberRepository;

    @Override
    public TokenDto login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(), request.password()
                    )
            );

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            redisService.setValuesWithTimeout("refreshToken:" + ((CustomUserDetails) authentication.getPrincipal()).getId(), refreshToken, Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidity()));
            return new TokenDto(accessToken, refreshToken);
        } catch(BadCredentialsException e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    public TokenDto reissue(String accessToken, String refreshToken) {

        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        String storedRefreshToken = (String) redisService.getValues("refreshToken:" + ((CustomUserDetails) authentication.getPrincipal()).getId());

        if(!(refreshToken.equals(storedRefreshToken))){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        redisService.setValuesWithTimeout("refreshToken:" + ((CustomUserDetails) authentication.getPrincipal()).getId(), newRefreshToken, Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidity()));

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    @Override
    public KakaoLoginResponse loginWithKakao(String code) {

        KakaoTokenResponse kakaoTokenResponse = kakaoClient.getAccessToken(code);
        KakaoProfileResponse kakaoProfileResponse = kakaoClient.getUserInfo(kakaoTokenResponse.accessToken());
        Member member = memberRepository.findByKakaoId(kakaoProfileResponse.id()).orElse(null);

        if(member == null) {
            return KakaoLoginResponse.of(kakaoProfileResponse.id());
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(member),
                null,
                List.of(() -> "")
        );

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        redisService.setValuesWithTimeout("refreshToken:" + ((CustomUserDetails) authentication.getPrincipal()).getId(), refreshToken, Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidity()));
        return KakaoLoginResponse.of(accessToken, refreshToken);
    }
}
