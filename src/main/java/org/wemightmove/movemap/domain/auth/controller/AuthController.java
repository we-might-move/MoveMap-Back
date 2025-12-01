package org.wemightmove.movemap.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.auth.dto.request.*;
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

    @Operation(summary = "회원가입", description = "")
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

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

    @Operation(summary = "카카오 로그인 액세스 토큰 전달", description = "카카오 로그인 액세스 토큰을 전달합니다. 가입된 회원이면 무브맵 서비스의 JWT 토큰을 발급해 응답하고, 미가입 회원이면 kakaoID를 응답합니다.")
    @PostMapping("/kakao")
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestBody KakaoLoginRequest request, HttpServletResponse response) {
        KakaoLoginResponse kakaoLoginResponse = authService.loginWithKakao(request);
        if(kakaoLoginResponse.isNewMember()) {
            return ResponseEntity.ok(new LoginResponse(kakaoLoginResponse.kakaoId(), kakaoLoginResponse.isNewMember()));
        } else {
            addCookie(response, "refreshToken", kakaoLoginResponse.refreshToken(), (int) jwtTokenProvider.getRefreshTokenValidity() / 1000);
            return ResponseEntity.ok(new LoginResponse(kakaoLoginResponse.accessToken(), kakaoLoginResponse.isNewMember()));
        }
    }

    @Operation(summary = "이메일 인증 코드 전송", description = "이메일 인증 코드를 전송합니다.")
    @PostMapping("/email/send")
    public ResponseEntity<Void> sendVerificationMail(@RequestBody @Valid SendVerificationMailRequest request) {
        authService.sendVerificationMail(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "사용자가 입력한 이메일 인증 코드가 올바른지 확인합니다.")
    @PostMapping("/email/verify")
    public ResponseEntity<Void> verify(@RequestBody @Valid VerifyRequest request) {
        authService.verifyCode(request);
        return ResponseEntity.ok().build();
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
