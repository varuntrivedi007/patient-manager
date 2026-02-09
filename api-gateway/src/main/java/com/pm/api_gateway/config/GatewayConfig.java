package com.pm.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        System.out.println("========================================");
        System.out.println("★★★ CONFIGURING GATEWAY ROUTES ★★★");
        System.out.println("========================================");

        return builder.routes()
                .route("auth-service-route", r -> {
                    System.out.println("★★★ Adding route: auth-service-route");
                    return r.path("/auth/**")
                            .filters(f -> f.stripPrefix(1))
                            .uri("http://auth-service:4005");
                })

                .route("patient-service-patients-exact", r -> {
                    System.out.println("★★★ Adding route: patient-service-patients-exact");
                    return r.path("/api/patients")
                            .filters(f -> f.stripPrefix(1))
                            .uri("http://patient-service:4000");
                })

                .route("patient-service-patients-wildcard", r -> {
                    System.out.println("★★★ Adding route: patient-service-patients-wildcard");
                    return r.path("/api/patients/**")
                            .filters(f -> f.stripPrefix(1))
                            .uri("http://patient-service:4000");
                })

                .route("api-docs-patient-route", r -> {
                    System.out.println("★★★ Adding route: api-docs-patient-route");
                    return r.path("/api-docs/patients")
                            .filters(f -> f.rewritePath("/api-docs/patients", "/v3/api-docs"))
                            .uri("http://patient-service:4000");
                })

                .build();
    }
}
