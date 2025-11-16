package org.wemightmove.movemap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.wemightmove.movemap.global.config.CorsConfigProperties;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(CorsConfigProperties.class)
public class MovemapApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovemapApplication.class, args);
    }

}
