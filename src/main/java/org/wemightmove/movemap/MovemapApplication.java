package org.wemightmove.movemap;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.wemightmove.movemap.global.config.CorsConfigProperties;
import org.wemightmove.movemap.global.config.EsProperties;
import org.wemightmove.movemap.global.config.SearchCacheProperties;
import org.wemightmove.movemap.global.config.SearchProperties;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties({CorsConfigProperties.class, EsProperties.class, SearchProperties.class, SearchCacheProperties.class})
@OpenAPIDefinition(
        servers = {
                @Server(url = "${springdoc.swagger-ui.production}", description = "Production Server"),
                @Server(url = "${springdoc.swagger-ui.development}", description = "Local Development Server")
        }
)
public class MovemapApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovemapApplication.class, args);
    }

}
