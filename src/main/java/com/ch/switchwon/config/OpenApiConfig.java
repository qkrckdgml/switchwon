package com.ch.switchwon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI switchwonOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("Switchwon API")
            .description("실시간 환율 기반 외환 주문 시스템 API")
            .version("v1.0.0"));
    }
}
