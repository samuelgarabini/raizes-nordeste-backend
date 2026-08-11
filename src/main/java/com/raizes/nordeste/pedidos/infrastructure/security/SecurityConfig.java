package com.raizes.nordeste.pedidos.infrastructure.config;

import com.raizes.nordeste.pedidos.infrastructure.security.JwtAuthenticationFilter;
import com.raizes.nordeste.pedidos.infrastructure.security.RestAccessDeniedHandler;
import com.raizes.nordeste.pedidos.infrastructure.security.RestAuthenticationEntryPoint;
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

    private final RestAuthenticationEntryPoint
        authenticationEntryPoint;

    private final RestAccessDeniedHandler
        accessDeniedHandler;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        RestAuthenticationEntryPoint authenticationEntryPoint,
        RestAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtAuthenticationFilter =
            jwtAuthenticationFilter;
        this.authenticationEntryPoint =
            authenticationEntryPoint;
        this.accessDeniedHandler =
            accessDeniedHandler;
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
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(
                        authenticationEntryPoint
                    )
                    .accessDeniedHandler(
                        accessDeniedHandler
                    )
            )
            .authorizeHttpRequests(auth -> auth
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
                    HttpMethod.PATCH,
                    "/api/v1/pedidos/*/cancelamento"
                )
                .hasAnyRole(
                    "ADMIN",
                    "GERENTE",
                    "ATENDENTE"
                )

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/fidelidade/*"
                )
                .hasAnyRole(
                    "ADMIN",
                    "GERENTE",
                    "ATENDENTE",
                    "CLIENTE"
                )

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/v1/fidelidade/*/consentimento"
                )
                .hasRole("CLIENTE")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/fidelidade/*/resgates"
                )
                .hasRole("CLIENTE")

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
