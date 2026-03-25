package org.example.crm.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

import java.util.List;

/**
 * Central CORS policy for the API.
 *
 * Why it's defined as a bean:
 * - Spring Security sits in front of MVC.
 * - Without a CorsConfigurationSource, preflight (OPTIONS) and
 *   requests with Authorization/If-Match headers will be rejected by the security chain.
 *
 * What we allow:
 * - Local dev UI origin (Vite)
 * - Methods used by the REST API (incl. PATCH/DELETE)
 * - Authorization + If-Match for JWT + optimistic locking
 *
 * Note:
 * - In production, restrict origins to the real frontend domains.
 */

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","If-Match","If-None-Match","Accept"));
        cfg.setExposedHeaders(List.of("ETag"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);

        return source;
    }
}