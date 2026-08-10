package com.raizes.nordeste.pedidos.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint
    implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter responseWriter;

    public RestAuthenticationEntryPoint(
        SecurityErrorResponseWriter responseWriter
    ) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        responseWriter.escrever(
            request,
            response,
            HttpStatus.UNAUTHORIZED,
            "NAO_AUTENTICADO",
            "É necessário fornecer uma autenticação válida"
        );
    }
}