package org.wemightmove.movemap.global.config;

import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }

    @Bean
    @Primary
    public FirebaseMessaging firebaseMessaging() {
        return mock(FirebaseMessaging.class);
    }

    // expoHttpClient는 각 테스트에서 @MockitoBean으로 관리

    @Bean
    @Primary
    public Executor pushExecutor() {
        return Runnable::run; // 테스트에서는 동기 실행
    }
}
