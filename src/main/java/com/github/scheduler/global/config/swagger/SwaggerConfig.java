package com.github.scheduler.global.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("My API")
                        .version("v1")
                        .description("JWT 인증이 적용된 Swagger 예제"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer scheduleExampleCustomizer() {
        return openApi -> {
            var pathItem = openApi.getPaths().get("/api/v1/schedules");
            if (pathItem != null && pathItem.getPost() != null) {
                pathItem.getPost()
                        .getRequestBody()
                        .getContent()
                        .get("application/json")
                        .addExamples("default", new Example().value("""
                    {
                      "createUserId": 0,
                      "calendarId": 0,
                      "title": "string",
                      "location": "string",
                      "startTime": "YYYY-MM-DD hh:mm:ss",
                      "endTime": "YYYY-MM-DD hh:mm:ss",
                      "repeatSchedule": {
                        "repeatType": "NONE(반복 없음)",
                        "repeatInterval": 0,
                        "repeatEndDate": null
                      },
                      "memo": "string",
                      "mentionUserIds": [0]
                    }
                    """));
            }
        };
    }

}
