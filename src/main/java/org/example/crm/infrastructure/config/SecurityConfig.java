package org.example.crm.infrastructure.config;

import static org.springframework.security.config.Customizer.withDefaults;
import org.example.crm.adapter.in.web.security.JwtService;
import org.example.crm.adapter.in.web.security.TokenFilter;
import org.example.crm.adapter.in.web.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 2, 65536, 3);
    }

    /**
     * Stateless security boundary.
     *
     * Model:
     * - JWT access token authenticates requests (no server session)
     * - RBAC via authorities (ROLE_ADMIN / ROLE_USER)
     * - Access gating (hasAccess) is enforced in AuthService on login/refresh
     *
     * Why CORS is enabled here:
     * - SecurityFilterChain must explicitly allow CORS so the browser can send
     *   Authorization / If-Match headers (otherwise preflight gets blocked).
     */

    @Bean
    public TokenFilter tokenFilter(JwtService jwtService) {
        return new TokenFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenFilter tokenFilter) throws Exception {
        return http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())
                .logout(l -> l.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        //  ADMIN ONLY
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // PUBLIC
                        .requestMatchers(
                                "/api/auth/**",
                                "/.well-known/jwks.json",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        //  EVERYTHING ELSE
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsServiceImpl userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
