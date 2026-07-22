package org.wemightmove.movemap.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.support.HttpHeaders;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Elasticsearch 클라이언트 설정.
 * <p>
 * {@link ElasticsearchConfiguration}을 상속하면 스프링 데이터가 저수준
 * {@code co.elastic.clients.elasticsearch.ElasticsearchClient} 빈을 포함해 필요한 빈들을 자동 구성한다
 * (GLOBAL CONSTRAINT §8 하이브리드 클라이언트 — bulk/타입드 쿼리/alias는 이 저수준 클라이언트를 직접 사용).
 * <p>
 * ⚠️ 로컬 perf 환경 ES는 xpack.security가 꺼져 있어 인증 없이 접속한다(GLOBAL CONSTRAINT §11).
 * 프로덕션은 보안이 켜져 있어야 하며, 이 경우 {@code movemap.es.username}/{@code password} 또는
 * {@code movemap.es.api-key}, 필요 시 {@code movemap.es.tls=true}를 설정해야 한다.
 * 인증/TLS는 값이 실제로 설정된 경우에만 적용된다(값이 없으면 평문·무인증 접속).
 */
@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final EsProperties esProperties;

    @Override
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder maybeSecureBuilder =
                ClientConfiguration.builder().connectedTo(esProperties.host());

        ClientConfiguration.TerminalClientConfigurationBuilder terminalBuilder =
                esProperties.tls() ? maybeSecureBuilder.usingSsl() : maybeSecureBuilder;

        terminalBuilder = terminalBuilder
                .withConnectTimeout(Duration.ofMillis(esProperties.connectTimeoutMs()))
                .withSocketTimeout(Duration.ofMillis(esProperties.socketTimeoutMs()));

        if (StringUtils.hasText(esProperties.apiKey())) {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "ApiKey " + esProperties.apiKey());
            terminalBuilder = terminalBuilder.withDefaultHeaders(headers);
        } else if (StringUtils.hasText(esProperties.username()) && StringUtils.hasText(esProperties.password())) {
            terminalBuilder = terminalBuilder.withBasicAuth(esProperties.username(), esProperties.password());
        }

        return terminalBuilder.build();
    }
}
