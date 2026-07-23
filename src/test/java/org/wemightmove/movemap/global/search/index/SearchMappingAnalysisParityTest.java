package org.wemightmove.movemap.global.search.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T6(7) — 매핑 파일 간 analysis 블록 정합성 순수 단위 테스트(컨테이너 불필요, T3 리뷰 지적 사항).
 * <p>
 * {@code program.json}/{@code facility.json} 은 서로 다른 매핑 파일이지만 {@code settings.analysis}
 * (tokenizer/filter/analyzer 정의)는 반드시 동일해야 한다 — 검색 품질(Nori 분석 결과)이 도메인 간 갈라지는
 * 것을 막기 위함이다. 이 테스트는 두 파일이 나란히 수정되지 않아 analysis 블록이 조용히 드리프트하는
 * 회귀를 잡는다.
 */
class SearchMappingAnalysisParityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void programAndFacilityMappings_haveByteIdenticalAnalysisBlock() throws IOException {
        JsonNode programAnalysis = readAnalysisBlock("es/mappings/program.json");
        JsonNode facilityAnalysis = readAnalysisBlock("es/mappings/facility.json");

        // 둘 다 실존해야 함 — 리소스 경로가 깨지거나 analysis 블록 자체가 사라지는 것도 회귀로 잡는다.
        assertThat(programAnalysis.isMissingNode()).isFalse();
        assertThat(facilityAnalysis.isMissingNode()).isFalse();
        assertThat(programAnalysis.isObject()).isTrue();

        assertThat(programAnalysis).isEqualTo(facilityAnalysis);
    }

    private JsonNode readAnalysisBlock(String classpath) throws IOException {
        try (InputStream is = new ClassPathResource(classpath).getInputStream()) {
            JsonNode root = MAPPER.readTree(is);
            return root.at("/settings/analysis");
        }
    }
}
