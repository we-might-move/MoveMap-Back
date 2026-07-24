package org.wemightmove.movemap.global.search.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.wemightmove.movemap.domain.program.dto.request.ProgramSearchByKeywordRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse;
import org.wemightmove.movemap.domain.program.dto.response.ProgramSimpleListResponse.ProgramSimpleItem;
import org.wemightmove.movemap.global.config.SearchCacheProperties;
import org.wemightmove.movemap.global.search.SearchMetrics;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * CT3 — 검색 캐시 계층({@link SearchResultCache}/{@link SearchCacheVersion}/{@link SearchCacheKey})을
 * ES/DB/앱 컨텍스트 없이 <b>격리</b>해서 검증한다(Testcontainers Redis + 알려진 값을 돌려주는 스텁 loader).
 * 동등성 회귀 테스트가 아니라 설계 §8 의 동작/정합성 계약(hit/miss·버전 무효화·negative·failover·2-tier·직렬화)을 검증한다.
 * <p>
 * 각 테스트는 {@code @BeforeEach} 에서 컨테이너를 flush 해 서로 격리된다(공유 컨테이너·독립 상태).
 */
class SearchResultCacheIT extends RedisCacheTestSupport {

    private StringRedisTemplate redis;
    private SimpleMeterRegistry registry;
    private SearchMetrics metrics;
    private ObjectMapper objectMapper;
    private SearchCacheProperties props;
    private SearchCacheVersion version;
    private SearchResultCache cache;

    @BeforeEach
    void setUp() {
        redis = newTemplate();
        redis.getConnectionFactory().getConnection().serverCommands().flushAll();

        registry = new SimpleMeterRegistry();
        metrics = new SearchMetrics(registry);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        props = new SearchCacheProperties(
                true,
                Duration.ofMinutes(10),
                Duration.ofSeconds(60),
                Duration.ofSeconds(30),
                new SearchCacheProperties.Local(false, 1000, Duration.ofSeconds(60))
        );

        version = new SearchCacheVersion(redis, props);
        cache = new SearchResultCache(redis, props, metrics, objectMapper, noLocalCacheProvider());
    }

    // ---------------------------------------------------------------------
    // 1) hit/miss
    // ---------------------------------------------------------------------

    @Test
    void hitMiss_secondCallServedFromCache_loaderCalledOnce() {
        String key = "mm:search:v0:program:es:test:none:20";
        ProgramSimpleListResponse expected = sampleResponse();
        AtomicInteger loaderCalls = new AtomicInteger();
        Supplier<ProgramSimpleListResponse> loader = countingLoader(loaderCalls, expected);

        ProgramSimpleListResponse first = cache.getOrLoad(key, ProgramSimpleListResponse.class, loader);
        ProgramSimpleListResponse second = cache.getOrLoad(key, ProgramSimpleListResponse.class, loader);

        assertThat(loaderCalls.get()).as("2번째 호출은 캐시 히트라 loader 재호출 없어야 함").isEqualTo(1);
        assertThat(first).isEqualTo(expected);
        assertThat(second).isEqualTo(expected);
        assertThat(registry.get("search_cache_miss_total").counter().count()).isEqualTo(1.0);
        assertThat(registry.get("search_cache_hit_total").counter().count()).isEqualTo(1.0);
    }

    // ---------------------------------------------------------------------
    // 2) 버전 무효화 — SearchCacheKey/SearchCacheVersion 을 그대로 써서 프로덕션과 동일하게 버전을 키에 반영
    // ---------------------------------------------------------------------

    @Test
    void versionInvalidation_bumpProducesUnreachableOldKey_loaderReinvoked() {
        ProgramSearchByKeywordRequest request = new ProgramSearchByKeywordRequest("수영", 20, null);
        ProgramSimpleListResponse expected = sampleResponse();
        AtomicInteger loaderCalls = new AtomicInteger();
        Supplier<ProgramSimpleListResponse> loader = countingLoader(loaderCalls, expected);

        int v0 = version.current();
        String keyV0 = SearchCacheKey.program(v0, "es", request);

        cache.getOrLoad(keyV0, ProgramSimpleListResponse.class, loader);
        cache.getOrLoad(keyV0, ProgramSimpleListResponse.class, loader);
        assertThat(loaderCalls.get()).as("bump 전 2회 조회는 1회만 loader 호출(캐시 히트)").isEqualTo(1);

        version.bump();
        int v1 = version.current();
        assertThat(v1).isEqualTo(v0 + 1);

        String keyV1 = SearchCacheKey.program(v1, "es", request);
        assertThat(keyV1).isNotEqualTo(keyV0);

        ProgramSimpleListResponse afterBump = cache.getOrLoad(keyV1, ProgramSimpleListResponse.class, loader);

        assertThat(loaderCalls.get()).as("새 버전 키는 옛 캐시에 도달 못 해 미스 → loader 재호출").isEqualTo(2);
        assertThat(afterBump).isEqualTo(expected);
    }

    // ---------------------------------------------------------------------
    // 3) negative caching
    // ---------------------------------------------------------------------

    @Test
    void negativeCaching_emptyResultStoredWithShortTtl_andServedFromCacheWithinTtl() {
        String key = "mm:search:v0:program:es:없는검색어:none:20";
        ProgramSimpleListResponse empty = ProgramSimpleListResponse.of(List.of(), null, false);
        AtomicInteger loaderCalls = new AtomicInteger();
        Supplier<ProgramSimpleListResponse> loader = countingLoader(loaderCalls, empty);

        ProgramSimpleListResponse first = cache.getOrLoad(key, ProgramSimpleListResponse.class, loader);
        assertThat(first.programs()).isEmpty();

        assertThat(redis.hasKey(key)).as("빈 결과도 Redis 에 저장돼야 함(negative caching)").isTrue();
        Long ttlSeconds = redis.getExpire(key, TimeUnit.SECONDS);
        assertThat(ttlSeconds).isNotNull();
        assertThat(ttlSeconds)
                .as("negative TTL(%ds) 이하로 짧게 저장돼야 함", props.negativeTtl().getSeconds())
                .isGreaterThan(0)
                .isLessThanOrEqualTo(props.negativeTtl().getSeconds());

        ProgramSimpleListResponse second = cache.getOrLoad(key, ProgramSimpleListResponse.class, loader);
        assertThat(loaderCalls.get()).as("negativeTtl 안의 재조회는 캐시 히트").isEqualTo(1);
        assertThat(second.programs()).isEmpty();
    }

    // ---------------------------------------------------------------------
    // 4) failover — Redis 가 죽어도 loader 결과가 예외 없이 반환되고 cacheError 가 증가
    // ---------------------------------------------------------------------

    @Test
    void failover_deadRedis_returnsLoaderResult_noException_andCacheErrorIncremented() {
        int deadPort = findUnusedPort();
        StringRedisTemplate deadRedis = newTemplate("127.0.0.1", deadPort);
        SearchResultCache deadCache = new SearchResultCache(deadRedis, props, metrics, objectMapper, noLocalCacheProvider());

        String key = "mm:search:v0:program:es:failover:none:20";
        ProgramSimpleListResponse expected = sampleResponse();
        AtomicInteger loaderCalls = new AtomicInteger();
        Supplier<ProgramSimpleListResponse> loader = countingLoader(loaderCalls, expected);

        ProgramSimpleListResponse result = deadCache.getOrLoad(key, ProgramSimpleListResponse.class, loader);

        assertThat(result).as("Redis 가 죽어도 예외 없이 loader 결과가 그대로 반환돼야 함").isEqualTo(expected);
        assertThat(loaderCalls.get()).isEqualTo(1);
        assertThat(registry.get("search_cache_error_total").counter().count())
                .as("Redis 접근 실패가 cacheError 로 계측돼야 함")
                .isGreaterThanOrEqualTo(1.0);
    }

    // ---------------------------------------------------------------------
    // 5) 2-tier — 로컬 히트 시 Redis 미조회, 버전 bump 시 로컬도 자동 무효
    // ---------------------------------------------------------------------

    @Test
    void twoTier_localHitServesWithoutRedis_versionBumpBypassesStaleLocalEntry() {
        Cache<String, String> local = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofSeconds(60))
                .build();
        SearchResultCache twoTierCache = new SearchResultCache(redis, props, metrics, objectMapper, localCacheProvider(local));

        ProgramSearchByKeywordRequest request = new ProgramSearchByKeywordRequest("요가", 20, null);
        ProgramSimpleListResponse expected = sampleResponse();
        AtomicInteger loaderCalls = new AtomicInteger();
        Supplier<ProgramSimpleListResponse> loader = countingLoader(loaderCalls, expected);

        int v0 = version.current();
        String keyV0 = SearchCacheKey.program(v0, "es", request);

        ProgramSimpleListResponse first = twoTierCache.getOrLoad(keyV0, ProgramSimpleListResponse.class, loader);
        assertThat(first).isEqualTo(expected);
        assertThat(loaderCalls.get()).isEqualTo(1);

        // Redis 에서 그 키를 직접 지워, 다음 조회가 "로컬만으로" 충족되는지 증명한다
        // (Redis 로 갔다면 미스 → loader 재호출됐을 것).
        redis.delete(keyV0);

        ProgramSimpleListResponse second = twoTierCache.getOrLoad(keyV0, ProgramSimpleListResponse.class, loader);
        assertThat(second).isEqualTo(expected);
        assertThat(loaderCalls.get()).as("로컬 히트라 Redis 를 안 거치고 loader 도 재호출 안 됨").isEqualTo(1);
        assertThat(registry.get("search_cache_local_hit_total").counter().count()).isEqualTo(1.0);

        // 버전 bump → 새 키는 로컬에도 없는 키(도달 불가) → 로컬/Redis 둘 다 미스 → loader 재호출
        version.bump();
        int v1 = version.current();
        String keyV1 = SearchCacheKey.program(v1, "es", request);
        assertThat(keyV1).isNotEqualTo(keyV0);

        ProgramSimpleListResponse afterBump = twoTierCache.getOrLoad(keyV1, ProgramSimpleListResponse.class, loader);
        assertThat(afterBump).isEqualTo(expected);
        assertThat(loaderCalls.get()).as("새 버전 키는 로컬도 미스 → loader 재호출").isEqualTo(2);
    }

    // ---------------------------------------------------------------------
    // 6) 직렬화 라운드트립 — 레코드 DTO 필드가 JSON 저장/역직렬화 후에도 보존되는지
    // ---------------------------------------------------------------------

    @Test
    void serializationRoundTrip_dtoFieldsPreservedThroughRedis() {
        String key = "mm:search:v0:program:es:roundtrip:none:20";
        ProgramSimpleListResponse original = new ProgramSimpleListResponse(
                List.of(
                        new ProgramSimpleItem(1L, "청소년 축구 교실", "강남종합체육관", "축구장", "서울특별시 강남구 테헤란로 123"),
                        new ProgramSimpleItem(2L, "성인 요가반", "송파구민체육센터", "체육관", "서울특별시 송파구 올림픽로 456")
                ),
                50L,
                true,
                2
        );

        // 1회차: loader 실행 결과가 Redis 에 JSON 직렬화되어 저장됨
        ProgramSimpleListResponse stored = cache.getOrLoad(key, ProgramSimpleListResponse.class, () -> original);
        assertThat(stored).isEqualTo(original);

        // 2회차: loader 는 절대 호출되지 않아야 함 — Redis 에 저장된 JSON 을 역직렬화한 값만으로 응답
        ProgramSimpleListResponse readBack = cache.getOrLoad(key, ProgramSimpleListResponse.class,
                () -> {
                    throw new AssertionError("캐시 히트여야 하므로 loader 가 호출되면 안 됨");
                });

        assertThat(readBack).isEqualTo(original);
        assertThat(readBack.programs()).containsExactlyElementsOf(original.programs());
        assertThat(readBack.nextCursor()).isEqualTo(original.nextCursor());
        assertThat(readBack.hasNext()).isEqualTo(original.hasNext());
        assertThat(readBack.currentSize()).isEqualTo(original.currentSize());
    }

    // ---------------------------------------------------------------------
    // fixtures
    // ---------------------------------------------------------------------

    private static ProgramSimpleListResponse sampleResponse() {
        return ProgramSimpleListResponse.of(
                List.of(new ProgramSimpleItem(1L, "수영 강습", "강남수영장", "수영장", "서울특별시 강남구")),
                null,
                false
        );
    }

    private static Supplier<ProgramSimpleListResponse> countingLoader(AtomicInteger calls, ProgramSimpleListResponse toReturn) {
        return () -> {
            calls.incrementAndGet();
            return toReturn;
        };
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<Cache<String, String>> noLocalCacheProvider() {
        ObjectProvider<Cache<String, String>> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        return provider;
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<Cache<String, String>> localCacheProvider(Cache<String, String> local) {
        ObjectProvider<Cache<String, String>> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(local);
        return provider;
    }
}
