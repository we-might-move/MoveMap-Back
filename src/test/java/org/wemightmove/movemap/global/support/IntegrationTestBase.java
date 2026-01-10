package org.wemightmove.movemap.global.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class IntegrationTestBase {

    private static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres");

    @Container
    private static final PostgreSQLContainer<?> POSTGIS =
            new PostgreSQLContainer<>(POSTGIS_IMAGE)
                    .withDatabaseName("movemap")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    private static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        // DataSource를 컨테이너로 강제
        registry.add("spring.datasource.url", POSTGIS::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGIS::getUsername);
        registry.add("spring.datasource.password", POSTGIS::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Flyway도 같은 DB를 보게 강제 (중요)
        registry.add("spring.flyway.url", POSTGIS::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGIS::getUsername);
        registry.add("spring.flyway.password", POSTGIS::getPassword);

        // Redis를 컨테이너로 강제
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));

        // (선택) 테스트에서만 JWT 더미값 넣어서 컨텍스트 로딩 실패 방지
        registry.add("jwt.secret", () -> "test-only-secret-test-only-secret-test-only-secret-1234567890");
        registry.add("jwt.access-token-validity", () -> "3600000");
        registry.add("jwt.refresh-token-validity", () -> "604800000");

        // ----------------------------
        // Kakao (더미)
        // ----------------------------
        registry.add("kakao.client-id", () -> "test-kakao-client-id");
        registry.add("kakao.redirect-uri", () -> "http://localhost/test/oauth/login/kakao");
        registry.add("kakao.token-uri", () -> "https://example.invalid/oauth/token");
        registry.add("kakao.user-info-uri", () -> "https://example.invalid/v2/user/me");

        // ----------------------------
        // 기타: 앱이 @Value로 주입받는다면 더미 필요
        // ----------------------------
        registry.add("url.signup", () -> "http://localhost/test");

        // Firebase: 실제 파일을 읽는 빈(@PostConstruct 등)이 있으면 테스트에서 깨질 수 있음
        // - 코드에서 실제로 파일을 읽는 구조라면, 아래처럼 더미로 두고
        //   테스트 프로필에서 해당 빈을 비활성화하는 방식이 더 안전함
        registry.add("firebase.config-path", () -> "dummy-firebase.json");

        // Mail: JavaMailSender를 실제로 구성하고 username/password를 @Value로 읽으면 필요
        // (실제 SMTP 연결을 시도하는 코드를 테스트에서 실행만 안 하면 됨)
        registry.add("mail.host", () -> "localhost");
        registry.add("mail.port", () -> "2525");
        registry.add("mail.username", () -> "test@example.com");
        registry.add("mail.password", () -> "test-password");
        registry.add("mail.templates.path", () -> "templates/email/");
        registry.add("mail.templates.img.logo", () -> "/static/logo.png");
        registry.add("mail.properties.mail.smtp.starttls.enable", () -> "true");
        registry.add("mail.properties.mail.smtp.auth", () -> "true");
        registry.add("mail.properties.mail.debug", () -> "false");

        // springdoc는 보통 테스트에 영향 없지만, 혹시라도 @Value가 있으면 대비
        registry.add("springdoc.swagger-ui.use-root-path", () -> "true");
        registry.add("springdoc.swagger-ui.production", () -> "http://localhost/test");
        registry.add("springdoc.swagger-ui.development", () -> "http://localhost/test");
        registry.add("springdoc.api-docs.enabled", () -> "true");
    }
}