package org.wemightmove.movemap.global.search.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ES 인덱스(`*_v1`) + alias(`*_search`)를 앱 기동 시 부트스트랩한다.
 * <p>
 * GLOBAL CONSTRAINT §8(하이브리드 클라이언트)에 따라 인덱스 생성/alias 조작은 저수준
 * {@link ElasticsearchClient}를 직접 사용한다. §10(패키지 관례)에 따라 도메인 무관 검색 인프라이므로
 * {@code global/search/index/}에 위치한다.
 * <p>
 * 멱등: 인덱스가 이미 존재하면 생성을 건너뛰고, alias가 이미 해당 인덱스를 가리키면 건드리지 않는다.
 * 문서 색인/리컨실리에이션은 T4 범위이며 이 클래스는 다루지 않는다.
 * <p>
 * {@code movemap.es.enabled=true}일 때만 활성화된다(기본 OFF). ES가 배제된 기본(db) 프로파일에서
 * 기동이 도달 불가능한 ES 호스트 때문에 실패하지 않도록 하기 위함이다 — 자세한 배경은
 * {@link org.wemightmove.movemap.global.config.EsProperties#enabled()} 참고.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "movemap.es.enabled", havingValue = "true", matchIfMissing = false)
@Order(0) // DDL/alias 보장 → 데이터 초기 색인(InitialIndexRunner, @Order(1))보다 먼저 실행되어야 함
@RequiredArgsConstructor
public class IndexBootstrapper implements ApplicationRunner {

    private static final List<SearchIndexDefinition> DEFINITIONS = List.of(
            new SearchIndexDefinition("program_v1", "program_search", "es/mappings/program.json"),
            new SearchIndexDefinition("facility_v1", "facility_search", "es/mappings/facility.json")
    );

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public void run(ApplicationArguments args) {
        for (SearchIndexDefinition definition : DEFINITIONS) {
            bootstrap(definition);
        }
    }

    private void bootstrap(SearchIndexDefinition definition) {
        try {
            ensureIndexCreated(definition);
            ensureAliasAssigned(definition);
        } catch (IOException e) {
            log.error("ES 인덱스 부트스트랩 실패: index={}, alias={}, mapping={}",
                    definition.indexName(), definition.aliasName(), definition.mappingPath(), e);
            throw new IllegalStateException("ES 인덱스 부트스트랩 실패: " + definition.indexName(), e);
        }
    }

    private void ensureIndexCreated(SearchIndexDefinition definition) throws IOException {
        boolean exists = elasticsearchClient.indices()
                .exists(e -> e.index(definition.indexName()))
                .value();

        if (exists) {
            log.info("ES 인덱스 이미 존재, 생성 스킵: {}", definition.indexName());
            return;
        }

        Resource mappingResource = new ClassPathResource(definition.mappingPath());
        try (InputStream mappingJson = mappingResource.getInputStream()) {
            elasticsearchClient.indices()
                    .create(c -> c.index(definition.indexName()).withJson(mappingJson));
            log.info("ES 인덱스 생성 완료: index={}, mapping={}", definition.indexName(), definition.mappingPath());
        }
    }

    private void ensureAliasAssigned(SearchIndexDefinition definition) throws IOException {
        boolean aliasExists = elasticsearchClient.indices()
                .existsAlias(e -> e.name(definition.aliasName()).index(definition.indexName()))
                .value();

        if (aliasExists) {
            log.info("ES alias 이미 인덱스를 가리킴, 스킵: {} -> {}", definition.aliasName(), definition.indexName());
            return;
        }

        elasticsearchClient.indices().updateAliases(u -> u
                .actions(a -> a.add(add -> add
                        .index(definition.indexName())
                        .alias(definition.aliasName())))
        );
        log.info("ES alias 연결 완료: {} -> {}", definition.aliasName(), definition.indexName());
    }
}
