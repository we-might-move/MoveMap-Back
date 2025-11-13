package org.wemightmove.movemap.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.auth.dto.request.LoginRequest;
import org.wemightmove.movemap.domain.auth.dto.response.LoginResponse;
import org.wemightmove.movemap.domain.auth.service.AuthService;
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

    @Operation(summary = "토큰 인증 테스트", description = "토큰 인증 테스트용 API입니다. 추후 삭제 예정입니다.")
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("OK");
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
