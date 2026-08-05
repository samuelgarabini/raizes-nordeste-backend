package com.raizes.nordeste.pedidos.presentation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleEnumError(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .error("VALOR_INVALIDO")
                .message("Corpo da requisição inválido ou valor de Enum não suportado.")
                .details(List.of(new ApiErrorResponse.FieldErrorDetail("canal", "Os valores aceitos para o canal são: APP, TOTEM, BALCAO, PICKUP, WEB.")))
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .requestId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ApiErrorResponse.FieldErrorDetail(err.getField(), err.getDefaultMessage()))
                .toList();

        ApiErrorResponse error = ApiErrorResponse.builder()
                .error("ERRO_VALIDACAO")
                .message("Erro de validação nos campos da requisição.")
                .details(details)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .requestId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericError(Exception ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .error("ERRO_INTERNO")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .requestId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}