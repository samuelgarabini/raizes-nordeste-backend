package com.raizes.nordeste.pedidos.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raizes.nordeste.pedidos.presentation.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(
        ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public void escrever(
        HttpServletRequest request,
        HttpServletResponse response,
        HttpStatus status,
        String error,
        String message
    ) throws IOException {
        ApiErrorResponse body = new ApiErrorResponse(
            error,
            message,
            null,
            Instant.now(),
            request.getRequestURI(),
            UUID.randomUUID().toString()
        );

        response.setStatus(status.value());
        response.setContentType(
            MediaType.APPLICATION_JSON_VALUE
        );
        response.setCharacterEncoding(
            StandardCharsets.UTF_8.name()
        );

        objectMapper.writeValue(
            response.getOutputStream(),
            body
        );
    }
}