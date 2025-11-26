package org.wemightmove.movemap.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config-path}")
    private String firebaseConfigPath;

    // Firebase 초기화 : 서버 시작 시 한 번만 실행됨
    @PostConstruct
public void init() {
    try {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase 이미 초기화됨");
            return;
        }

        log.info("Firebase config path = {}", firebaseConfigPath);

        File file = new File(firebaseConfigPath);
        log.info("File exists = {}", file.exists());
        log.info("File canRead = {}", file.canRead());
        log.info("Absolute path = {}", file.getAbsolutePath());

        InputStream serviceAccount = new FileInputStream(file);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase 초기화 완료");

    } catch (Exception e) {
        log.error("Firebase 초기화 실패", e);
        throw new RuntimeException("Firebase 초기화 실패", e);
    }
}
}
