package org.wemightmove.movemap.global.search;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 검색 색인/리컨실리에이션 관측용 Micrometer 메트릭 홀더.
 * <p>
 * GLOBAL CONSTRAINT §6(정합성 관측: 로그+메트릭) 을 위한 단일 컴포넌트. T5 가 fallback/latency 메트릭을
 * 여기에 추가할 예정이므로 확장 가능하게 둔다(카운터/게이지 등록 헬퍼 재사용).
 * <ul>
 *   <li>{@code search_bulk_item_failures_total} — bulk 개별 항목 실패 누적 카운터</li>
 *   <li>{@code search_reconciliation_drift{domain}} — PG count - ES count 게이지</li>
 *   <li>{@code search_reconciliation_last_success{domain}} — 마지막 성공 리컨실 epoch second 게이지</li>
 * </ul>
 * 게이지는 {@code domain} 태그별로 상태 홀더({@link AtomicLong})를 지연 등록한다.
 */
@Component
public class SearchMetrics {

    private static final String BULK_ITEM_FAILURES = "search_bulk_item_failures_total";
    private static final String RECONCILIATION_DRIFT = "search_reconciliation_drift";
    private static final String RECONCILIATION_LAST_SUCCESS = "search_reconciliation_last_success";
    private static final String ES_FALLBACK = "search_es_fallback_total";
    private static final String ES_LATENCY = "search_es_latency";
    private static final String DOMAIN_TAG = "domain";

    private final MeterRegistry registry;
    private final Counter bulkItemFailures;
    private final Map<String, AtomicLong> driftGauges = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> lastSuccessGauges = new ConcurrentHashMap<>();
    private final Map<String, Counter> esFallbackCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> esLatencyTimers = new ConcurrentHashMap<>();

    public SearchMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.bulkItemFailures = Counter.builder(BULK_ITEM_FAILURES)
                .description("bulk 색인 응답에서 개별 항목이 실패한 누적 횟수")
                .register(registry);
    }

    /** bulk 응답의 개별 항목 실패 1건을 계수한다(에러는 절대 삼키지 않음 — 호출부에서 ERROR 로그 병행). */
    public void incrementBulkItemFailure() {
        bulkItemFailures.increment();
    }

    /** 도메인별 드리프트(= PG count - ES count)를 게이지에 반영한다. */
    public void recordDrift(String domain, long drift) {
        gauge(driftGauges, RECONCILIATION_DRIFT, domain).set(drift);
    }

    /** 도메인별 마지막 리컨실 성공 시각(epoch second)을 게이지에 반영한다. */
    public void recordReconciliationSuccess(String domain, long epochSecond) {
        gauge(lastSuccessGauges, RECONCILIATION_LAST_SUCCESS, domain).set(epochSecond);
    }

    /**
     * ES 검색이 실패해 DB 로 fallback 한 1건을 도메인별로 계수한다
     * ({@code search_es_fallback_total{domain=program|facility}}). 조용한 다운그레이드 금지(GLOBAL §3):
     * 호출부에서 WARN 로그(원문 아닌 keyword 길이) 를 병행한다.
     */
    public void esFallback(String domain) {
        esFallbackCounters.computeIfAbsent(domain, d -> Counter.builder(ES_FALLBACK)
                        .description("ES 검색 실패로 DB 로 fallback 한 누적 횟수")
                        .tag(DOMAIN_TAG, d)
                        .register(registry))
                .increment();
    }

    /**
     * ES 검색 호출 지연을 기록하는 도메인별 타이머({@code search_es_latency{domain}}).
     * 호출부에서 {@code searchMetrics.esLatency(domain).record(() -> esAdapter.search(...))} 로 감싼다.
     */
    public Timer esLatency(String domain) {
        return esLatencyTimers.computeIfAbsent(domain, d -> Timer.builder(ES_LATENCY)
                .description("ES 검색 호출 지연")
                .tag(DOMAIN_TAG, d)
                .register(registry));
    }

    private AtomicLong gauge(Map<String, AtomicLong> holders, String meterName, String domain) {
        return holders.computeIfAbsent(domain, d -> {
            AtomicLong holder = new AtomicLong(0);
            Gauge.builder(meterName, holder, AtomicLong::get)
                    .tag(DOMAIN_TAG, d)
                    .register(registry);
            return holder;
        });
    }
}
