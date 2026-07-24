package org.wemightmove.movemap.global.search.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.global.config.SearchCacheProperties;
import org.wemightmove.movemap.global.search.SearchMetrics;

import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 검색 결과 cache-aside 계층(설계 §4.7). 서비스 impl 과 {@code SearchEngineRouter} 사이에 얇게 끼운다.
 * <p>
 * {@link #getOrLoad}: 캐시 히트면 역직렬화해 반환, 미스면 {@code loader}(ES/DB 검색) 실행 후 JSON 으로 저장한다.
 * 빈 결과는 짧은 negative TTL, 정상 결과는 {@code ttl + 지터(0~jitter)} 로 저장한다(스탬피드 방지, 설계 §4.3/§4.5).
 * <p>
 * <b>failover(설계 §4.6)</b>: 모든 Redis 접근을 try/catch 로 감싸 예외 시 WARN 로그 + {@link SearchMetrics#cacheError()}
 * 후 loader 로 우회한다. 캐시에서 절대 예외를 던지지 않는다(검색은 캐시 없이도 동작해야 한다).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchResultCache {

    private final StringRedisTemplate redis;
    private final SearchCacheProperties props;
    private final SearchMetrics metrics;
    private final ObjectMapper objectMapper;

    /**
     * cache-aside 조회. {@code cache.enabled=false} 면 그냥 {@code loader.get()}(캐시 완전 우회).
     *
     * @param key    캐시 키({@link SearchCacheKey})
     * @param type   역직렬화 대상 응답 타입
     * @param loader 미스/장애 시 실행할 원본 검색(라우터 호출)
     */
    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader) {
        if (!props.enabled()) {
            return loader.get();
        }

        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                T value = objectMapper.readValue(cached, type);
                metrics.cacheHit();
                return value;
            }
            metrics.cacheMiss();
        } catch (Exception e) {
            metrics.cacheError();
            log.warn("검색 캐시 read 실패 → loader 우회 key={}", key, e);
            return loader.get();
        }

        T result = loader.get();
        store(key, result);
        return result;
    }

    private <T> void store(String key, T result) {
        try {
            Duration ttl = isEmptyResult(result)
                    ? props.negativeTtl()
                    : props.ttl().plusSeconds(jitterSeconds());
            redis.opsForValue().set(key, objectMapper.writeValueAsString(result), ttl);
        } catch (Exception e) {
            metrics.cacheError();
            log.warn("검색 캐시 write 실패 → 무시 key={}", key, e);
        }
    }

    /** 0 ~ jitter(초) 사이 랜덤 가산(동시 만료 스탬피드 방지). jitter 가 0 이하면 0. */
    private long jitterSeconds() {
        long max = props.jitter().getSeconds();
        return max <= 0 ? 0 : ThreadLocalRandom.current().nextLong(max + 1);
    }

    /**
     * 응답 DTO 의 첫 번째 컬렉션(리스트) 필드가 비었는지로 "빈 결과" 를 일반적으로 판별한다.
     * (ProgramSimpleListResponse#programs, FacilitySimpleListResponse#facilities). 판별 실패 시 비어있지 않음으로 본다.
     */
    private boolean isEmptyResult(Object result) {
        if (result == null) {
            return true;
        }
        try {
            RecordComponent[] components = result.getClass().getRecordComponents();
            if (components != null) {
                for (RecordComponent component : components) {
                    if (Collection.class.isAssignableFrom(component.getType())) {
                        Object value = component.getAccessor().invoke(result);
                        return value == null || ((Collection<?>) value).isEmpty();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("빈 결과 판별 실패 → 정상 TTL 적용 type={}", result.getClass().getSimpleName(), e);
        }
        return false;
    }
}
