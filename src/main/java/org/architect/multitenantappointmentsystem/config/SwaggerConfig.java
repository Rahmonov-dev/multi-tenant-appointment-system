package org.architect.multitenantappointmentsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .addSecurityItem(
                        new SecurityRequirement().addList("bearerAuth")
                );
    }

    @Bean
    public OpenApiCustomizer tenantHeaderCustomizer() {
        return openApi ->
                openApi.getPaths().forEach((path, pathItem) -> {

                    // Tenant ID kerak bo'lmagan endpointlar ro'yxati
                    boolean skipTenantId =
                                    path.contains("/api/auth") ||
                                    path.contains("/swagger") ||
                                    path.contains("/api/tenant/get-all") ||
                                    path.contains("/api/tenant/{slug}/by-key/{tenantKey}") ||
                                    path.contains("/api/tenant") ||
                                    path.contains("/api-docs");


                    if (!skipTenantId) {
                        pathItem.readOperations().forEach(operation ->
                                operation.addParametersItem(
                                        new Parameter()
                                                .in("header")
                                                .required(true)
                                                .name("X-Tenant-Id")
                                                .description("Tenant ID (Get from login response)")
                                                .schema(new StringSchema())
                                                .example("1")  // ← Default qiymat
                                )
                        );
                    }
                });
    }
}