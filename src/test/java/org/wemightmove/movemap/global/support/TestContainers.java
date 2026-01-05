package org.wemightmove.movemap.global.support;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainers {

    private static final PostgreSQLContainer<?> POSTGRES;
    private static final GenericContainer<?> REDIS;

    static {
        POSTGRES = new PostgreSQLContainer<>(
                DockerImageName.parse("postgis/postgis:16-3.4")
                        .asCompatibleSubstituteFor("postgres")
        )
                .withDatabaseName("movemap")
                .withUsername("admin")
                .withPassword("password");

        // Redis
        REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

        // 컨테이너 시작
        POSTGRES.start();
        REDIS.start();
    }

    public static PostgreSQLContainer<?> getPostgres() {
        return POSTGRES;
    }

    public static GenericContainer<?> getRedis() {
        return REDIS;
    }
}
