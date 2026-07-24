package org.wemightmove.movemap.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Elasticsearch 클라이언트 설정.
 * <p>
 * 로컬 perf 환경의 ES는 xpack.security가 꺼져 있어 인증 없이 접속한다(GLOBAL CONSTRAINT §11).
 * username/password/apiKey/tls는 프로덕션(보안 ON) 대비 옵션으로만 존재하며, 값이 있을 때만 적용한다.
 * ⚠️ 프로덕션에서는 보안이 켜져 있어야 하며, 이 경우 username/password 또는 apiKey를 반드시 설정할 것.
 *
 * @param enabled          ES 기동 인프라(인덱스 부트스트랩, 초기 색인, 리컨실 스케줄러) 활성화 여부.
 *                          기본 false — {@code host} 기본값(localhost:9200)이 어느 환경에나 존재하지 않으므로,
 *                          기본 db 검색 프로파일이 ES 부재로 기동 실패하지 않게 하기 위한 옵트인 스위치다.
 *                          {@code movemap.es.enabled=true}인 프로파일(perf 등)에서만 ES 스타트업 컴포넌트가
 *                          동작한다. {@link ElasticsearchConfig}의 클라이언트 빈 자체는 lazy(생성 시 미접속)라
 *                          이 플래그와 무관하게 항상 등록된다.
 * @param host             ES 호스트:포트 (예: localhost:9200)
 * @param username         basic-auth 사용자명 (옵션, 값 있을 때만 적용)
 * @param password         basic-auth 비밀번호 (옵션, 값 있을 때만 적용)
 * @param apiKey           API Key 인증 (옵션, 값 있을 때만 적용, username/password보다 우선)
 * @param tls              TLS(HTTPS) 사용 여부. 기본 false(로컬 perf ES는 평문 HTTP)
 * @param connectTimeoutMs 연결 타임아웃(ms). 기본 2000
 * @param socketTimeoutMs  소켓(응답) 타임아웃(ms). 기본 5000
 */
@ConfigurationProperties(prefix = "movemap.es")
public record EsProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("localhost:9200") String host,
        String username,
        String password,
        String apiKey,
        @DefaultValue("false") boolean tls,
        @DefaultValue("2000") int connectTimeoutMs,
        @DefaultValue("5000") int socketTimeoutMs
) {
}
