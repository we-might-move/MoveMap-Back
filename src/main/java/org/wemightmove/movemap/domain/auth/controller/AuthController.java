package org.wemightmove.movemap.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.auth.dto.request.LoginRequest;
import org.wemightmove.movemap.domain.auth.dto.response.KakaoLoginResponse;
import org.wemightmove.movemap.domain.auth.dto.response.LoginResponse;
import org.wemightmove.movemap.domain.auth.service.AuthService;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.jwt.JwtTokenProvider;
import org.wemightmove.movemap.global.jwt.TokenDto;

@RestController
@Tag(name = "Auth")
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @Value("${url.signup}")
    private String signupUrl;

    @Operation(summary = "자체 로그인", description = "서비스 자체 로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response){
        TokenDto tokens = authService.login(request);
        addCookie(response, "refreshToken", tokens.refreshToken(), (int) jwtTokenProvider.getRefreshTokenValidity() / 1000);
        return ResponseEntity.ok(new LoginResponse(tokens.accessToken()));
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @PostMapping("/logout")
    public void logout() {

    }

    @Operation(summary = "토큰 재발급", description = "액세스 토큰 만료 시 리프레시 토큰을 이용해 액세스 토큰과 리프레시 토큰을 재발급합니다.")
    @PostMapping("/token")
    public ResponseEntity<LoginResponse> reissue(HttpServletRequest request, HttpServletResponse response){
        String accessToken = getAccessToken(request);
        String refreshToken = getCookie(request, "refreshToken");
        TokenDto tokens = authService.reissue(accessToken, refreshToken);
        addCookie(response, "refreshToken", tokens.refreshToken(), (int) jwtTokenProvider.getRefreshTokenValidity() / 1000);
        return ResponseEntity.ok(new LoginResponse(tokens.accessToken()));
    }

    @Operation(summary = "카카오 로그인 인가 코드 전달", description = "카카오 로그인 인가 코드를 전달합니다. 가입된 회원이면 토큰을 발급해 응답하고, 미가입 회원이면 회원 가입 페이지로 리다이렉트시킵니다.")
    @PostMapping("/kakao")
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
        KakaoLoginResponse kakaoLoginResponse = authService.loginWithKakao(code);
        if(kakaoLoginResponse.isNewMember()) {
            String redirectUrl = signupUrl + "?kakaoId=" + kakaoLoginResponse.kakaoId();
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } else {
            addCookie(response, "refreshToken", kakaoLoginResponse.refreshToken(), (int) jwtTokenProvider.getRefreshTokenValidity() / 1000);
            return ResponseEntity.ok(new LoginResponse(kakaoLoginResponse.accessToken()));
        }
    }

    @Operation(summary = "토큰 인증 테스트", description = "토큰 인증 테스트용 API입니다. 추후 삭제 예정입니다.")
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            throw new CustomException(ErrorCode.NO_COOKIE);
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        throw new CustomException(ErrorCode.NO_COOKIE);
    }

    private String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken == null || !bearerToken.startsWith("Bearer ")){
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return bearerToken.substring(7);
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);

        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);
    }
}
