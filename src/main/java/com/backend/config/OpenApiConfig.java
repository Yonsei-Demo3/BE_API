package com.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(
            new Info()
                .title("BE API")
                .description("의견 공유 서비스 API 문서")
                .version("v1.0.0")
                .contact(new Contact().name("EHHO").email("example@yourmail.com"))
        );
    }
}
