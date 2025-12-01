package org.wemightmove.movemap.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.auth.dto.request.KakaoLoginRequest;
import org.wemightmove.movemap.domain.auth.dto.request.LoginRequest;
import org.wemightmove.movemap.domain.auth.dto.request.SignupRequest;
import org.wemightmove.movemap.domain.auth.dto.response.KakaoLoginResponse;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.global.client.KakaoClient;
import org.wemightmove.movemap.global.client.dto.KakaoProfileResponse;
import org.wemightmove.movemap.global.enums.SignupType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.jwt.JwtTokenProvider;
import org.wemightmove.movemap.global.jwt.TokenDto;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;
import org.wemightmove.movemap.global.security.CustomUserDetails;
import org.wemightmove.movemap.global.util.RedisService;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final KakaoClient kakaoClient;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegionTypeRepository regionTypeRepository;


    @Override
    @Transactional
    public void signup(SignupRequest request) {
        if(request.type().equals(SignupType.LOCAL)) {
            localSignup(request);
        } else if(request.type().equals(SignupType.KAKAO)) {
            kakaoSignup(request);
        }
    }

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
    public KakaoLoginResponse loginWithKakao(KakaoLoginRequest request) {

        KakaoProfileResponse kakaoProfileResponse = kakaoClient.getUserInfo(request.accessToken());
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

    private void localSignup(SignupRequest request) {
        if(request.email() == null || request.password() == null || memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .age(request.age())
                .height(request.height())
                .weight(request.weight())
                .sex(request.sexType())
                .isDeleted(false)
                .role(request.role())
                .uuid(createUuid())
                .regionCode(getRegionCode(request.sido(), request.sigungu()))
                .build();

        memberRepository.save(member);
    }

    private void kakaoSignup(SignupRequest request) {
        if(request.kakaoId() == null || memberRepository.existsByKakaoId(request.kakaoId())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        Member member = Member.builder()
                .role(request.role())
                .kakaoId(request.kakaoId())
                .nickname(request.nickname())
                .age(request.age())
                .height(request.height())
                .weight(request.weight())
                .sex(request.sexType())
                .isDeleted(false)
                .uuid(createUuid())
                .regionCode(getRegionCode(request.sido(), request.sigungu()))
                .build();

        memberRepository.save(member);
    }

    private String createUuid() {
        String uuid = UUID.randomUUID().toString().replace("-","");
        while(memberRepository.existsByUuid(uuid)) {
            uuid = UUID.randomUUID().toString().replace("-","");
        }
        return uuid;
    }

    private String getRegionCode(String sido, String sigungu) {
        return regionTypeRepository.findRegionByNameAndParentName(sigungu, sido)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST)).getPrefix();
    }


}
