package org.wemightmove.movemap.global.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 검색 엔진 라우팅/fallback 단일 지점(GLOBAL §3).
 * <p>
 * {@code engine=es} 면 ES supplier 를 latency 타이머로 감싸 실행하고, 어떤 예외든 발생 시
 * {@link SearchMetrics#esFallback(String)} 계수 + WARN 로그(원문 아닌 keyword 길이, 스택트레이스 보존) 후
 * DB supplier 로 fallback 한다. {@code engine!=es} 면 바로 DB supplier 를 실행한다.
 * <p>
 * program/facility 서비스가 동일하게 재사용해 fallback 로직이 한 곳에만 존재하도록 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEngineRouter {

    private static final String ENGINE_ES = "es";

    private final SearchMetrics searchMetrics;

    /**
     * @param engine        도메인별 엔진 flag({@code es|db})
     * @param domain        메트릭/로그 태그용 도메인 라벨
     * @param keywordLength 로그에 남길 keyword 길이(원문은 남기지 않음)
     * @param es            ES 검색 실행 supplier
     * @param db            DB 검색(fallback/기본) 실행 supplier
     */
    public <T> T route(String engine, String domain, int keywordLength, Supplier<T> es, Supplier<T> db) {
        if (ENGINE_ES.equalsIgnoreCase(engine)) {
            try {
                return searchMetrics.esLatency(domain).record(es::get);
            } catch (Exception e) {
                searchMetrics.esFallback(domain);
                log.warn("ES search fallback→DB domain={} keyword_len={}", domain, keywordLength, e);
                return db.get();
            }
        }
        return db.get();
    }
}
