package org.wemightmove.movemap.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.wemightmove.movemap.domain.auth.dto.request.*;
import org.wemightmove.movemap.domain.auth.dto.response.KakaoLoginResponse;
import org.wemightmove.movemap.domain.auth.dto.response.LoginResponse;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.global.client.KakaoClient;
import org.wemightmove.movemap.global.client.dto.KakaoProfileResponse;
import org.wemightmove.movemap.global.enums.SignupType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.jwt.JwtTokenProvider;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;
import org.wemightmove.movemap.global.security.CustomUserDetails;
import org.wemightmove.movemap.global.util.RedisService;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    @Value("${mail.username}")
    private String fromMail;
    @Value("${mail.templates.img.logo}")
    private String logoPath;

    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final KakaoClient kakaoClient;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegionTypeRepository regionTypeRepository;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private static final String TEMP_PASSWORD_CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";


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
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(), request.password()
                    )
            );

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            redisService.setValuesWithTimeout("refreshToken:" + ((CustomUserDetails) authentication.getPrincipal()).getId(), refreshToken, Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidity()));
            return new LoginResponse(accessToken, refreshToken);
        } catch(BadCredentialsException e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    public LoginResponse reissue(String accessToken, String refreshToken) {

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

        return new LoginResponse(newAccessToken, newRefreshToken);
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

    @Override
    public void sendVerificationMail(SendVerificationMailRequest request) {
        String toMail = request.email();

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        String title = "[MoveMap] 이메일 인증 코드 전송"; //이메일 제목

        HashMap<String,Object> map = new HashMap<>();
        map.put("code", uuid);

        Context context = new Context();
        context.setVariables(map); //템플릿에 전달할 데이터
        String content = templateEngine.process("verification.html", context);

        redisService.setValuesWithTimeout("verification_code:"+toMail, uuid, Duration.ofMinutes(10));

        sendEmail(toMail,title,content);
    }

    @Override
    public void verifyCode(VerifyRequest request) {
        String code = request.code();
        String key = "verification_code:" + request.email();

        Object value = redisService.getValues(key);

        if (value == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        if (!value.toString().equals(code)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        redisService.deleteValues(key);
    }

    @Override
    @Transactional
    public void sendTemporaryPassword(TemporaryPasswordRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String temporaryPassword = generateTemporaryPassword(12);
        member.changePassword(passwordEncoder.encode(temporaryPassword));

        String title = "[MoveMap] 임시 비밀번호 전송"; //이메일 제목

        HashMap<String,Object> map = new HashMap<>();
        map.put("password", temporaryPassword);

        Context context = new Context();
        context.setVariables(map); //템플릿에 전달할 데이터
        String content = templateEngine.process("tempPassword.html", context);

        sendEmail(request.email(), title, content);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Member member = getCurrentMember();
        if(!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            System.out.println("1");
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        if(!request.newPassword().equals(request.confirmPassword())) {
            System.out.println("2");
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        member.changePassword(passwordEncoder.encode(request.newPassword()));
        memberRepository.save(member);
    }

    private String generateTemporaryPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(TEMP_PASSWORD_CHAR_SET.length());
            sb.append(TEMP_PASSWORD_CHAR_SET.charAt(idx));
        }
        return sb.toString();
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

    private void sendEmail(String toMail, String title, String content){
        try{
            MimeMessage mailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true, "UTF-8");
            helper.setFrom(fromMail);
            helper.setTo(toMail);
            helper.setSubject(title);
            helper.setText(content, true);
            helper.addInline("logo", new ClassPathResource(logoPath));

            javaMailSender.send(mailMessage);
        } catch (MessagingException e){
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return memberRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

}
