package org.wemightmove.movemap.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * 검색 결과 Redis 캐시(Stage 1) 및 로컬 2-tier 캐시(Stage 2 예약) 설정.
 * <p>
 * 설계 §4.8/§5.3. 캐시는 선택 계층이므로 기본 {@code enabled=false} 로 두고 플래그로 켠다(롤아웃 안전).
 *
 * @param enabled     Redis 캐시 활성화 여부(기본 false)
 * @param ttl         정상 결과 캐시 TTL(기본 10분)
 * @param jitter      TTL 지터 상한 — 0~jitter 초 랜덤 가산으로 동시 만료 스탬피드 방지(기본 60초)
 * @param negativeTtl 빈 결과(negative) 캐시 TTL(기본 30초)
 * @param local       Stage 2 로컬(Caffeine) 캐시 설정 — 지금은 자리만 정의, 배선은 CT2
 */
@ConfigurationProperties(prefix = "movemap.search.cache")
public record SearchCacheProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("10m") Duration ttl,
        @DefaultValue("60s") Duration jitter,
        @DefaultValue("30s") Duration negativeTtl,
        @DefaultValue Local local
) {

    /**
     * Stage 2 로컬 캐시 설정(설계 §5.3). CT2에서 실제 배선한다.
     *
     * @param enabled 로컬 캐시 활성화 여부(기본 false)
     * @param maxSize 로컬 캐시 최대 엔트리 수(기본 1000 — 뜨거운 소수만)
     * @param ttl     로컬 캐시 expireAfterWrite(기본 60초)
     */
    public record Local(
            @DefaultValue("false") boolean enabled,
            @DefaultValue("1000") int maxSize,
            @DefaultValue("60s") Duration ttl
    ) {
    }
}
