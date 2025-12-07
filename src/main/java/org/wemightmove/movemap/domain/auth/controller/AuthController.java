package org.wemightmove.movemap.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@Tag(name = "Auth")
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = """
            - type : LOCAL / KAKAO
            - role : STUDENT / PARENT
            - email, password : type 값이 LOCAL인 경우만 필수 입력
            - kakaoId : type 값이 KAKAO인 경우만 필수 입력
            - nickname : 2자 이상 20자 이하. 한글, 영문, 숫자만 가능(공백, 특수문자 불가)
            - sido, sigungu : 서비스 데이터베이스에 존재하는 값이랑 일치해야 함
            - sexType : WOMAN / MAN
            - age : int 값
            - height, weight : double 값
            """
    )
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자체 로그인", description = "이메일과 패스워드를 이용해 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 성공 응답이 오면 저장한 토큰 및 로그인 관련 상태 정보를 모두 삭제해주세요.")
    @PostMapping("/logout")
    public void logout() {
    }

    @Operation(summary = "토큰 재발급", description = "액세스 토큰 만료 시 리프레시 토큰을 이용해 액세스 토큰과 리프레시 토큰을 재발급합니다.")
    @PostMapping("/token")
    public ResponseEntity<LoginResponse> reissue(HttpServletRequest request, @RequestBody @Valid ReissueTokenRequest reissueTokenRequest){
        String accessToken = getAccessToken(request);
        String refreshToken = reissueTokenRequest.refreshToken();
        return ResponseEntity.ok(authService.reissue(accessToken, refreshToken));
    }

    @Operation(summary = "카카오 로그인 액세스 토큰 전달", description = "카카오 로그인 액세스 토큰을 전달합니다. 가입된 회원이면 무브맵 서비스의 JWT 토큰을 발급해 응답하고, 미가입 회원이면 kakaoID를 응답합니다.")
    @PostMapping("/kakao")
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestBody KakaoLoginRequest request, HttpServletResponse response) {
        KakaoLoginResponse kakaoLoginResponse = authService.loginWithKakao(request);
        if(kakaoLoginResponse.isNewMember()) {
            return ResponseEntity.ok(new LoginResponse(kakaoLoginResponse.kakaoId(), kakaoLoginResponse.isNewMember()));
        } else {
            return ResponseEntity.ok(new LoginResponse(kakaoLoginResponse.accessToken(), kakaoLoginResponse.refreshToken(), kakaoLoginResponse.isNewMember()));
        }
    }

    @Operation(summary = "이메일 인증 코드 전송", description = "자체 회원가입 시 이메일 인증 코드를 전송합니다.")
    @PostMapping("/email/send")
    public ResponseEntity<Void> sendVerificationMail(@RequestBody @Valid SendVerificationMailRequest request) {
        authService.sendVerificationMail(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "자체 회원가입 시 사용자가 입력한 이메일 인증 코드가 일치하는지 검사합니다.")
    @PostMapping("/email/verify")
    public ResponseEntity<Void> verify(@RequestBody @Valid VerifyRequest request) {
        authService.verifyCode(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "임시 비밀번호 발송", description = "사용자의 비밀번호를 임의로 변경하고 사용자의 이메일로 바뀐 비밀번호를 발송합니다.")
    @PostMapping("/email/password")
    public ResponseEntity<Void> sendTemporaryPassword(@RequestBody @Valid TemporaryPasswordRequest request) {
        authService.sendTemporaryPassword(request);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "비밀번호 재설정", description = "로그인한 사용자의 비밀번호를 변경합니다.")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "토큰 인증 테스트",
            description = "토큰 인증 테스트용 API입니다. 추후 삭제 예정입니다."
    )
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken == null || !bearerToken.startsWith("Bearer ")){
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return bearerToken.substring(7);
    }
}
