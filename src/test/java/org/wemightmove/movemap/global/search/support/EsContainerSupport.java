package org.wemightmove.movemap.global.search.support;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T6 안전망 테스트 공용 베이스: {@code perf/es/Dockerfile}(elasticsearch:8.18.3 + analysis-nori)로
 * 빌드한 Testcontainers ES 컨테이너를 JVM 전체에서 <b>단 한 번</b> 기동해 모든 ES 통합 테스트 클래스가
 * 공유한다(static 필드 — 하위 클래스가 몇 개든 static 초기화 블록은 클래스 최초 로딩 시 1회만 실행).
 * <p>
 * 실제 앱(Spring context)이나 프로덕션 DB는 띄우지 않는다 — 저수준 {@link ElasticsearchClient}만으로
 * 검색 어댑터(POJO, {@code @RequiredArgsConstructor})를 직접 생성해 테스트한다.
 * <p>
 * GLOBAL §12(테스트 전략): intent/contract/fallback/injection 위주, 옛 결과와의 동등성 회귀는 만들지 않는다.
 */
public abstract class EsContainerSupport {

    private static final Path DOCKERFILE_PATH = Paths.get("perf/es/Dockerfile");

    protected static final GenericContainer<?> ES_CONTAINER;
    protected static final ElasticsearchClient ES_CLIENT;

    static {
        ImageFromDockerfile image = new ImageFromDockerfile("movemap-test-es-nori", false)
                .withDockerfile(DOCKERFILE_PATH);

        ES_CONTAINER = new GenericContainer<>(image)
                .withExposedPorts(9200)
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false")
                .withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
                .waitingFor(Wait.forHttp("/").forPort(9200).forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)));
        ES_CONTAINER.start();

        HttpHost host = new HttpHost(ES_CONTAINER.getHost(), ES_CONTAINER.getMappedPort(9200), "http");
        RestClient restClient = RestClient.builder(host).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ES_CLIENT = new ElasticsearchClient(transport);
    }

    /**
     * 컨테이너에 nori 플러그인이 실제로 설치돼 있는지 매 서브클래스 로드 시 확인한다(브리프 요구사항).
     * 컨테이너는 static 이라 1회만 뜨지만, 이 검증 자체는 각 서브클래스에서 재실행해도 무해하고 저렴하다.
     */
    @BeforeAll
    static void verifyNoriPluginInstalled() throws IOException {
        Request request = new Request("GET", "/_cat/plugins");
        Response response = rawRestClientRequest(request);
        String body = EntityUtils.toString(response.getEntity());
        assertThat(body).as("analysis-nori 플러그인이 ES 컨테이너에 설치되어 있어야 함").contains("analysis-nori");
    }

    private static Response rawRestClientRequest(Request request) throws IOException {
        HttpHost host = new HttpHost(ES_CONTAINER.getHost(), ES_CONTAINER.getMappedPort(9200), "http");
        try (RestClient restClient = RestClient.builder(host).build()) {
            return restClient.performRequest(request);
        }
    }

    /** 주어진 인덱스명으로, main 리소스의 실제 매핑 JSON({@code es/mappings/*.json})을 그대로 사용해 인덱스를 생성한다(멱등). */
    protected static void createIndexFromMapping(String indexName, String mappingClasspath) throws IOException {
        boolean exists = ES_CLIENT.indices().exists(e -> e.index(indexName)).value();
        if (exists) {
            return;
        }
        try (InputStream mappingJson = new ClassPathResource(mappingClasspath).getInputStream()) {
            ES_CLIENT.indices().create(c -> c.index(indexName).withJson(mappingJson));
        }
    }

    /** 색인 직후 검색 가능하도록 refresh. */
    protected static void refresh(String indexName) throws IOException {
        ES_CLIENT.indices().refresh(r -> r.index(indexName));
    }

    protected static <T> void bulkIndex(String indexName, List<IdDoc<T>> docs) throws IOException {
        ES_CLIENT.bulk(bulk -> {
            for (IdDoc<T> doc : docs) {
                bulk.operations(op -> op.index(idx -> idx
                        .index(indexName)
                        .id(String.valueOf(doc.id()))
                        .document(doc.body())));
            }
            return bulk;
        });
        refresh(indexName);
    }

    /** id + 색인 문서 페어(테스트 픽스처 빌더 편의용). */
    public record IdDoc<T>(long id, T body) {
    }
}
