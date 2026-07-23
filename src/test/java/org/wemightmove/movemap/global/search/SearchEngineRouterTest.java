package org.wemightmove.movemap.global.search;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T6(6) — {@link SearchEngineRouter} fallback 계측(GLOBAL §3) 순수 단위 테스트(컨테이너 불필요).
 * <p>
 * ES supplier 가 실패하면: (a) DB supplier 결과가 반환되고, (b) {@link SearchMetrics} fallback 카운터가
 * 증가하고, (c) WARN 로그가 남되 keyword 길이만 남고 원문은 아예 로그 호출부에 존재하지 않음(구조적으로
 * 원문이 새어나갈 방법이 없음을, 포맷된 로그 메시지 검사로 재확인)을 검증한다.
 */
class SearchEngineRouterTest {

    private Logger routerLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        routerLogger = (Logger) LoggerFactory.getLogger(SearchEngineRouter.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        routerLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        routerLogger.detachAppender(logAppender);
    }

    @Test
    void esFailure_fallsBackToDb_incrementsMetric_andLogsWarnWithoutRawKeyword() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SearchMetrics searchMetrics = new SearchMetrics(registry);
        SearchEngineRouter router = new SearchEngineRouter(searchMetrics);

        String rawKeyword = "수영장민감정보";
        String dbResult = "DB_RESULT";

        String result = router.route(
                "es",
                "program",
                rawKeyword.length(),
                () -> { throw new IllegalStateException("ES down"); },
                () -> dbResult
        );

        // (a) DB fallback 결과 반환
        assertThat(result).isEqualTo(dbResult);

        // (b) 메트릭 카운터 증가
        assertThat(registry.get("search_es_fallback_total").tag("domain", "program").counter().count())
                .isEqualTo(1.0);

        // (c) WARN 로그 1건, keyword 길이는 있고 원문은 로그 호출부에 애초에 전달되지 않음(구조적 안전).
        assertThat(logAppender.list).hasSize(1);
        ILoggingEvent event = logAppender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(event.getFormattedMessage())
                .contains("domain=program")
                .contains("keyword_len=" + rawKeyword.length())
                .doesNotContain(rawKeyword);
        assertThat(event.getThrowableProxy()).isNotNull();
        assertThat(event.getThrowableProxy().getMessage()).isEqualTo("ES down");
    }

    @Test
    void engineNotEs_neverCallsEsSupplier_noFallbackMetered() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SearchMetrics searchMetrics = new SearchMetrics(registry);
        SearchEngineRouter router = new SearchEngineRouter(searchMetrics);

        String result = router.route(
                "db",
                "facility",
                5,
                () -> { throw new AssertionError("ES supplier must not run when engine=db"); },
                () -> "DB_ONLY"
        );

        assertThat(result).isEqualTo("DB_ONLY");
        assertThat(registry.find("search_es_fallback_total").counter()).isNull();
        assertThat(logAppender.list).isEmpty();
    }

    @Test
    void esSuccess_returnsEsResult_noFallbackMetered() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SearchMetrics searchMetrics = new SearchMetrics(registry);
        SearchEngineRouter router = new SearchEngineRouter(searchMetrics);

        String result = router.route("es", "program", 3, () -> "ES_RESULT", () -> "DB_RESULT");

        assertThat(result).isEqualTo("ES_RESULT");
        assertThat(registry.find("search_es_fallback_total").counter()).isNull();
        assertThat(logAppender.list).isEmpty();
    }
}
