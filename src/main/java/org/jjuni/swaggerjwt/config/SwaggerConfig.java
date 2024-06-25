package org.jjuni.swaggerjwt.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    private static final String SECURITY_SCHEME_NAME = "authorization";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        // JWT 사용s
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .info(new Info()
                        .title("Springboot+Swagger+JWT")
                        .version("1.0")
                        .description("초기 구축 셋팅 과정을 기록하는 swagger 입니다."));
    }

    // path, package 별 그룹 나누기
//    @Bean
//    public GroupedOpenApi api() {
//        String[] paths = {"/api/v1/**"};
//        String[] packagesToScan = {""};
//        return GroupedOpenApi.builder().group("springdoc-openapi")
//                .pathsToMatch(paths)
//                .packagesToScan(packagesToScan)
//                .build();
//    }
}