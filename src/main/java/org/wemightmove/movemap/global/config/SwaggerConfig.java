package org.wemightmove.movemap.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "MoveMap API Docs", version = "v1")
)
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

//    @Bean
//    public OpenAPI openAPI() {
//        SecurityScheme bearerAuth = new SecurityScheme()
//                .type(SecurityScheme.Type.HTTP)
//                .scheme("bearer")
//                .bearerFormat("JWT")
//                .in(SecurityScheme.In.HEADER)
//                .name(HttpHeaders.AUTHORIZATION);
//
//        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");
//
//        return new OpenAPI()
//                .components(new Components()
//                        .addSecuritySchemes("bearerAuth", bearerAuth))
//                .security(Arrays.asList(securityRequirement));
//    }
//
//
//    @Bean
//    public GroupedOpenApi chatOpenApi() {
//        String[] paths = {"/**"};
//
//        return GroupedOpenApi.builder()
//                .group("we-might-move")
//                .pathsToMatch(paths)
//                .build();
//    }
}