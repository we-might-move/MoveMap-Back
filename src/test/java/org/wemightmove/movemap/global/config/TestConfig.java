package org.wemightmove.movemap.global.config;

import com.google.firebase.messaging.FirebaseMessaging;
import okhttp3.OkHttpClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
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

    @Bean
    @Primary
    public OkHttpClient expoHttpClient() {
        return mock(OkHttpClient.class);
    }

    @Bean
    @Primary
    public Executor pushExecutor() {
        return Runnable::run; // 동기 실행
    }
}
