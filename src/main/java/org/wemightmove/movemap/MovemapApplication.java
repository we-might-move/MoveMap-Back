package org.wemightmove.movemap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MovemapApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovemapApplication.class, args);
    }

}
