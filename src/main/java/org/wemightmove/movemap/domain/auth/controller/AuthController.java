package org.wemightmove.movemap.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.auth.dto.request.LoginRequest;
import org.wemightmove.movemap.domain.auth.dto.response.LoginResponse;
import org.wemightmove.movemap.domain.auth.service.AuthService;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.jwt.JwtTokenProvider;
import org.wemightmove.movemap.global.jwt.TokenDto;
import org.wemightmove.movemap.global.util.RedisService;

@RestController
@Tag(name = "Auth")
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final RedisService redisService;

    @Operation(summary = "자체 로그인", description = "서비스 자체 로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response){
        TokenDto tokens = authService.login(request);
        addCookie(response, "refreshToken", tokens.refreshToken(), (int) jwtTokenProvider.getRefreshTokenValidity());
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
        addCookie(response, "refreshToken", tokens.refreshToken(), (int) jwtTokenProvider.getRefreshTokenValidity());
        return ResponseEntity.ok(new LoginResponse(tokens.accessToken()));
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
