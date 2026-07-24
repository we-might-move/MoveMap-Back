package org.wemightmove.movemap.global.search.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
 * 검색 결과 cache-aside 계층(설계 §4.7, Stage 2 2-tier는 §5.2/§5.4). 서비스 impl 과
 * {@code SearchEngineRouter} 사이에 얇게 끼운다.
 * <p>
 * {@link #getOrLoad}: **로컬(Caffeine) → Redis → loader** 순으로 조회한다(설계 §5.2).
 * 로컬 히트면 Redis 를 아예 조회하지 않고 즉시 반환한다. 로컬 미스·Redis 히트면 로컬에도 채운다.
 * 둘 다 미스면 {@code loader}(ES/DB 검색) 실행 후 Redis + 로컬 양쪽에 저장한다. 빈 결과는 짧은
 * negative TTL, 정상 결과는 {@code ttl + 지터(0~jitter)} 로 Redis 에 저장한다(스탬피드 방지, 설계 §4.3/§4.5).
 * <p>
 * 로컬 계층은 {@code movemap.search.cache.local.enabled=true} 일 때만 존재한다({@link LocalSearchCacheConfig}).
 * 빈이 없으면({@code ObjectProvider} 가 {@code null} 반환) 로컬 단계를 통째로 건너뛰어 Stage 1(Redis 전용)과
 * 완전히 동일하게 동작한다(회귀 없음).
 * <p>
 * **버전 무효화가 로컬에도 자동 적용되는 이유**: 캐시 키 문자열 자체에 {@code v{ver}} 가 포함되어 있어,
 * 재색인으로 전역 버전이 오르면 새 요청은 새 버전 키만 조회한다 → 로컬의 옛 버전 엔트리는 아무도 찾지
 * 않게 되고(도달 불가) 이후 Caffeine {@code expireAfterWrite} 로 청소된다. 별도 로컬 무효화 로직 불필요.
 * <p>
 * <b>failover(설계 §4.6)</b>: 로컬/Redis 접근을 모두 try/catch 로 감싸 예외 시 WARN 로그 +
 * {@link SearchMetrics#cacheError()} 후 다음 계층(로컬 실패→Redis, Redis 실패→loader)으로 우회한다.
 * 캐시에서 절대 예외를 던지지 않는다(검색은 캐시 없이도 동작해야 한다).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchResultCache {

    private final StringRedisTemplate redis;
    private final SearchCacheProperties props;
    private final SearchMetrics metrics;
    private final ObjectMapper objectMapper;
    /** {@code local.enabled=false} 면 빈 자체가 없으므로 {@link ObjectProvider#getIfAvailable()} 가 null 반환. */
    private final ObjectProvider<Cache<String, String>> localCacheProvider;

    /**
     * 2-tier cache-aside 조회. {@code cache.enabled=false} 면 그냥 {@code loader.get()}(캐시 완전 우회).
     *
     * @param key    캐시 키({@link SearchCacheKey})
     * @param type   역직렬화 대상 응답 타입
     * @param loader 미스/장애 시 실행할 원본 검색(라우터 호출)
     */
    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader) {
        if (!props.enabled()) {
            return loader.get();
        }

        Cache<String, String> local = localCacheProvider.getIfAvailable();

        String localHit = readLocal(local, key);
        if (localHit != null) {
            try {
                T value = objectMapper.readValue(localHit, type);
                metrics.cacheHit();
                metrics.cacheLocalHit();
                return value;
            } catch (Exception e) {
                metrics.cacheError();
                log.warn("검색 로컬 캐시 역직렬화 실패 → Redis 계속 진행 key={}", key, e);
            }
        }

        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                T value = objectMapper.readValue(cached, type);
                metrics.cacheHit();
                writeLocal(local, key, cached);
                return value;
            }
            metrics.cacheMiss();
        } catch (Exception e) {
            metrics.cacheError();
            log.warn("검색 캐시 read 실패 → loader 우회 key={}", key, e);
            return loader.get();
        }

        T result = loader.get();
        store(key, result, local);
        return result;
    }

    /** 로컬 캐시 조회. 로컬 계층이 없거나(비활성) 예외 발생 시 {@code null}(= Redis로 계속 진행). */
    private String readLocal(Cache<String, String> local, String key) {
        if (local == null) {
            return null;
        }
        try {
            return local.getIfPresent(key);
        } catch (Exception e) {
            metrics.cacheError();
            log.warn("검색 로컬 캐시 read 실패 → Redis 계속 진행 key={}", key, e);
            return null;
        }
    }

    /** Redis 히트/loader 결과를 로컬에도 채운다(§5.2). 로컬 계층이 없거나 예외가 나도 응답에는 영향 없다. */
    private void writeLocal(Cache<String, String> local, String key, String json) {
        if (local == null) {
            return;
        }
        try {
            local.put(key, json);
        } catch (Exception e) {
            metrics.cacheError();
            log.warn("검색 로컬 캐시 write 실패 → 무시 key={}", key, e);
        }
    }

    private <T> void store(String key, T result, Cache<String, String> local) {
        String json;
        try {
            json = objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            metrics.cacheError();
            log.warn("검색 캐시 write 실패(직렬화) → 무시 key={}", key, e);
            return;
        }

        try {
            Duration ttl = isEmptyResult(result)
                    ? props.negativeTtl()
                    : props.ttl().plusSeconds(jitterSeconds());
            redis.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            metrics.cacheError();
            log.warn("검색 캐시 write 실패 → 무시 key={}", key, e);
        }

        writeLocal(local, key, json);
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
