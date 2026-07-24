package org.wemightmove.movemap.global.search.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wemightmove.movemap.global.config.SearchCacheProperties;

/**
 * 검색 결과 로컬(Caffeine) 캐시 — Stage 2 2-tier(설계 §5.3). {@code movemap.search.cache.local.enabled=true}
 * 일 때만 {@link Cache} 빈을 생성한다(기본 off — 롤아웃 안전, CT1 Redis 전용 동작과 동등해야 하므로).
 * <p>
 * {@code maximumSize}/{@code expireAfterWrite} 는 {@link SearchCacheProperties.Local} 에서 읽는다.
 * 캐시 키에 이미 {@code v{ver}} 가 포함되므로, 재색인으로 전역 버전이 오르면 옛 버전 로컬 엔트리는
 * 자동으로 도달 불가가 되고 TTL 로 청소된다(별도 무효화 로직 불필요, 설계 §5.3).
 * <p>
 * 이 빈이 없을 때({@code local.enabled=false}) {@link SearchResultCache} 는 로컬 계층을 건너뛰고
 * Stage 1(Redis 전용) 과 완전히 동일하게 동작한다({@code ObjectProvider} 로 옵셔널 주입).
 */
@Configuration
@ConditionalOnProperty(name = "movemap.search.cache.local.enabled", havingValue = "true")
public class LocalSearchCacheConfig {

    @Bean
    public Cache<String, String> localSearchCache(SearchCacheProperties props) {
        SearchCacheProperties.Local local = props.local();
        return Caffeine.newBuilder()
                .maximumSize(local.maxSize())
                .expireAfterWrite(local.ttl())
                .recordStats()
                .build();
    }
}
