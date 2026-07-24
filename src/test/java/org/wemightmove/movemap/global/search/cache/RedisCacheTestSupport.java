package org.wemightmove.movemap.global.search.cache;

import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;

/**
 * CT3 공용 베이스: JVM 전체에서 <b>단 한 번</b> 기동하는 Testcontainers Redis 컨테이너를 모든 캐시
 * 테스트 클래스가 공유한다({@code EsContainerSupport}와 동일한 static-container 관례). 캐시 계층
 * ({@link SearchResultCache}/{@link SearchCacheVersion})만 격리 테스트하므로 실제 Spring context 는
 * 띄우지 않고, {@link StringRedisTemplate}을 POJO로 직접 구성한다.
 * <p>
 * failover 테스트(§4.6)를 위해 커맨드/커넥트 타임아웃을 짧게(500ms/300ms) 잡아, 죽은 포트로의 접속
 * 실패가 테스트를 오래 블록시키지 않게 한다(설계 문서의 "짧은 Redis 타임아웃 필수" 전제와 동일한 이유).
 */
abstract class RedisCacheTestSupport {

    protected static final RedisContainer REDIS_CONTAINER;

    static {
        REDIS_CONTAINER = new RedisContainer("7-alpine");
        REDIS_CONTAINER.start();
    }

    /** 컨테이너를 가리키는 정상 동작하는 {@link StringRedisTemplate}. */
    protected static StringRedisTemplate newTemplate() {
        return newTemplate(REDIS_CONTAINER.getHost(), REDIS_CONTAINER.getMappedPort(6379));
    }

    /** 임의 host/port 를 가리키는 {@link StringRedisTemplate}(failover 테스트용 — 죽은 포트 등). */
    protected static StringRedisTemplate newTemplate(String host, int port) {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(500))
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .connectTimeout(Duration.ofMillis(300))
                                .build())
                        .autoReconnect(false)
                        .build())
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, clientConfig);
        factory.afterPropertiesSet();

        StringRedisTemplate template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        return template;
    }

    /** 아무 것도 리스닝하지 않는 로컬 포트를 하나 확보한다(failover 테스트에서 "죽은 Redis" 를 흉내). */
    protected static int findUnusedPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("사용 가능한 로컬 포트를 찾지 못함", e);
        }
    }
}
