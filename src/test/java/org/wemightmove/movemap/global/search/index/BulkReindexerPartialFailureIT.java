package org.wemightmove.movemap.global.search.index;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.wemightmove.movemap.global.search.SearchMetrics;
import org.wemightmove.movemap.global.search.support.EsContainerSupport;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T6(8, 선택) — 부분 bulk 실패 표면화 검증(GLOBAL §6: {@code BulkResponse.errors()} 개별 항목 검사 필수,
 * 빈 catch 금지). {@link BulkReindexer#bulkIndex}에 정상 문서 1건 + {@code dynamic:strict} 매핑을 위반하는
 * (매핑에 없는 필드를 가진) 문서 1건을 함께 보내, 실패 건수가 삼켜지지 않고 반환값·메트릭·로그 3곳 모두에
 * 표면화되는지 확인한다.
 * <p>
 * {@link BulkReindexer}는 리컨실 델타 조회 등에서만 {@code EntityManager}가 필요하고, {@code SearchCacheVersion}은
 * {@code reindexAll()}(전량 재색인 완료 훅)에서만 쓰인다. {@code bulkIndex} 자체는 둘 다 사용하지 않으므로, 이
 * 테스트에서는 {@code entityManager=null}/{@code searchCacheVersion=null}로 직접 생성한다(Spring 컨텍스트 불필요).
 */
class BulkReindexerPartialFailureIT extends EsContainerSupport {

    private static final String INDEX_NAME = SearchDomain.PROGRAM.indexName(); // "program_v1"

    private Logger bulkReindexerLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeAll
    static void setUpIndex() throws IOException {
        createIndexFromMapping(INDEX_NAME, "es/mappings/program.json");
    }

    @BeforeEach
    void attachLogAppender() {
        bulkReindexerLogger = (Logger) LoggerFactory.getLogger(BulkReindexer.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        bulkReindexerLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        bulkReindexerLogger.detachAppender(logAppender);
    }

    @Test
    void bulkIndex_partialFailure_isNotSwallowed_surfacedInReturnValue_metric_andLog() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SearchMetrics searchMetrics = new SearchMetrics(registry);
        BulkReindexer bulkReindexer = new BulkReindexer(null, ES_CLIENT, searchMetrics, null);

        long goodId = 9001L;
        long badId = 9002L;
        ProgramDoc good = ProgramDoc.of(goodId, "정상프로그램", "정상시설", "정상종목", "서울", Instant.now());
        RogueProgramDoc bad = new RogueProgramDoc(badId, "불량프로그램", "hash", "매핑에없는필드값");

        long failedCount = bulkReindexer.bulkIndex(SearchDomain.PROGRAM, List.of(good, bad));

        // 1) 반환값에 실패 건수가 표면화됨(삼켜지지 않음)
        assertThat(failedCount).isEqualTo(1L);

        // 2) 메트릭에 반영됨
        assertThat(registry.get("search_bulk_item_failures_total").counter().count()).isEqualTo(1.0);

        // 3) ERROR 로그로 표면화됨(빈 catch 아님)
        boolean hasErrorLog = logAppender.list.stream().anyMatch(e -> e.getLevel() == Level.ERROR);
        assertThat(hasErrorLog).isTrue();

        // 정상 문서는 실제로 색인됨(정상 항목까지 함께 실패 처리되지 않음)
        assertThatDocIndexed(goodId);
    }

    private void assertThatDocIndexed(long id) {
        try {
            boolean exists = ES_CLIENT.exists(e -> e.index(INDEX_NAME).id(String.valueOf(id))).value();
            assertThat(exists).as("정상 문서(id=%d)는 실제로 색인되어야 함", id).isTrue();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** {@code dynamic:strict} 매핑에 없는 필드를 담은, 고의로 잘못된 테스트 전용 문서. */
    private record RogueProgramDoc(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("content_hash") String contentHash,
            @JsonProperty("unexpected_field") String unexpectedField
    ) implements IndexedDoc {
    }
}
