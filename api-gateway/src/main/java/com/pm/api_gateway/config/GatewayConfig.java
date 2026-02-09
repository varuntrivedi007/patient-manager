package com.pm.api_gateway.config;

import com.pm.api_gateway.filter.JwtValidationGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(
            RouteLocatorBuilder builder,
            JwtValidationGatewayFilterFactory jwtValidation // <-- your filter factory
    ) {
        System.out.println("========================================");
        System.out.println("★★★ CONFIGURING GATEWAY ROUTES ★★★");
        System.out.println("========================================");

        return builder.routes()

                // /auth/** -> auth-service (StripPrefix=1)
                .route("auth-service-route", r -> {
                    System.out.println("★★★ Adding route: auth-service-route");
                    return r.path("/auth/**")
                            .filters(f -> f.stripPrefix(1))
                            .uri("http://auth-service:4005");
                })

                // /api/patients/** -> patient-service (StripPrefix=1 + JwtValidation)
                .route("patient-service-route", r -> {
                    System.out.println("★★★ Adding route: patient-service-route");
                    return r.path("/api/patients/**")
                            .filters(f -> f
                                    .stripPrefix(1)
                                    // matches YAML: - JwtValidation
                                    .filter(jwtValidation.apply(new Object()))
                            )
                            .uri("http://patient-service:4000");
                })

                // /api-docs/patients -> patient-service /v3/api-docs
                .route("api-docs-patient-route", r -> {
                    System.out.println("★★★ Adding route: api-docs-patient-route");
                    return r.path("/api-docs/patients")
                            .filters(f -> f.rewritePath("/api-docs/patients", "/v3/api-docs"))
                            .uri("http://patient-service:4000");
                })

                // /api-docs/auth -> auth-service /v3/api-docs
                .route("api-docs-auth-route", r -> {
                    System.out.println("★★★ Adding route: api-docs-auth-route");
                    return r.path("/api-docs/auth")
                            .filters(f -> f.rewritePath("/api-docs/auth", "/v3/api-docs"))
                            .uri("http://auth-service:4005");
                })

                .build();
    }
}
