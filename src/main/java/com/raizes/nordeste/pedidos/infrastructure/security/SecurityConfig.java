package com.raizes.nordeste.pedidos.infrastructure.config;

import com.raizes.nordeste.pedidos.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter
        jwtAuthenticationFilter;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter =
            jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http
    ) throws Exception {

        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )
            .authorizeHttpRequests(auth -> auth
                /*
                 * Endpoints públicos: autenticação,
                 * cardápio, documentação e erros.
                 */
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/unidades/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error"
                )
                .permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/campanhas/**"
                )
                .hasAnyRole(
                    "ADMIN",
                    "GERENTE",
                    "ATENDENTE",
                    "CLIENTE"
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/campanhas/**"
                )
                .hasAnyRole(
                    "ADMIN",
                    "GERENTE"
                )

                /*
                 * Esta regra específica deve aparecer
                 * antes da regra genérica dos pedidos.
                 */
                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/v1/pedidos/*/status"
                )
                .hasAnyRole(
                    "ADMIN",
                    "GERENTE",
                    "ATENDENTE"
                )

                .requestMatchers(
                    "/api/v1/pedidos/**"
                )
                .hasAnyRole(
                    "ADMIN",
                    "GERENTE",
                    "ATENDENTE",
                    "CLIENTE"
                )

                .anyRequest()
                .authenticated()
            )
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}