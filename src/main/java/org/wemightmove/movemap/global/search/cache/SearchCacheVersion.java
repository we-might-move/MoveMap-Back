package org.wemightmove.movemap.global.search.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.global.config.SearchCacheProperties;

/**
 * 검색 캐시 전역 버전(설계 §4.4) — Redis 키 {@code mm:search:version}(정수) 하나로 무효화를 처리한다.
 * <p>
 * 캐시 키에 {@code v{ver}} 가 붙으므로, 재색인이 끝나고 {@link #bump()}({@code INCR}) 하면 옛 버전 키는
 * 즉시 도달 불가가 되고 TTL 로 청소된다(SCAN/DEL 불필요).
 * <p>
 * <b>failover</b>: 캐시가 꺼져 있거나 Redis 가 죽어도 절대 예외를 던지지 않는다. 버전을 못 읽으면
 * sentinel {@code 0} 을 반환해 검색이 그대로 동작하게 한다(설계 §4.6).
 * <p>
 * {@link #current()} 는 매 요청 Redis read 를 피하려고 버전 값을 ~5초 로컬 캐시한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchCacheVersion {

    static final String VERSION_KEY = "mm:search:version";
    /** Redis 미가용/비활성 시 사용하는 sentinel 버전. */
    static final int SENTINEL_VERSION = 0;
    private static final long LOCAL_TTL_MILLIS = 5_000L;

    private final StringRedisTemplate redis;
    private final SearchCacheProperties props;

    private volatile int cachedVersion = SENTINEL_VERSION;
    private volatile long cachedAtMillis = 0L;

    /**
     * 현재 전역 버전을 반환한다. 최근 {@value #LOCAL_TTL_MILLIS}ms 안에 읽은 값이 있으면 그대로 쓰고,
     * 아니면 Redis 에서 다시 읽는다. 캐시 비활성/Redis 실패 시 {@value #SENTINEL_VERSION} 로 우회한다.
     */
    public int current() {
        if (!props.enabled()) {
            return SENTINEL_VERSION;
        }
        long now = System.currentTimeMillis();
        if (now - cachedAtMillis < LOCAL_TTL_MILLIS) {
            return cachedVersion;
        }
        try {
            String value = redis.opsForValue().get(VERSION_KEY);
            int version = value == null ? SENTINEL_VERSION : Integer.parseInt(value);
            cachedVersion = version;
            cachedAtMillis = now;
            return version;
        } catch (Exception e) {
            log.warn("검색 캐시 버전 read 실패 → sentinel({}) 사용", SENTINEL_VERSION, e);
            return SENTINEL_VERSION;
        }
    }

    /**
     * 전역 버전을 1 증가시킨다(재색인 완료 훅에서 호출). 캐시 비활성이면 no-op, Redis 실패 시 로그만 남기고 삼키지 않는다.
     * 성공 시 로컬 캐시를 무효화해 다음 {@link #current()} 가 새 값을 즉시 읽게 한다.
     */
    public void bump() {
        if (!props.enabled()) {
            return;
        }
        try {
            Long newVersion = redis.opsForValue().increment(VERSION_KEY);
            cachedAtMillis = 0L; // 다음 current() 강제 재조회
            log.info("검색 캐시 버전 bump 완료: version={}", newVersion);
        } catch (Exception e) {
            log.warn("검색 캐시 버전 bump(INCR) 실패 → 무효화 스킵(TTL 로 청소)", e);
        }
    }
}
