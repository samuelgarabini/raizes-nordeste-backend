package com.raizes.nordeste.pedidos.infrastructure.exception;

import com.raizes.nordeste.pedidos.presentation.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
        ApiException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = createResponse(
            exception.getErrorCode(),
            exception.getMessage(),
            null,
            request,
            UUID.randomUUID().toString()
        );

        return ResponseEntity
            .status(exception.getStatus())
            .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        List<ApiErrorResponse.FieldErrorDetail> details =
            exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiErrorResponse.FieldErrorDetail(
                    error.getField(),
                    error.getDefaultMessage()
                ))
                .toList();

        ApiErrorResponse response = createResponse(
            "VALIDATION_ERROR",
            "Erro de validação nos campos da requisição",
            details,
            request,
            UUID.randomUUID().toString()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
        Exception exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = createResponse(
            "INVALID_REQUEST",
            "O conteúdo ou os parâmetros da requisição são inválidos",
            null,
            request,
            UUID.randomUUID().toString()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(
        Exception exception,
        HttpServletRequest request
    ) {
        String requestId = UUID.randomUUID().toString();

        log.error(
            "Erro interno. requestId={}, path={}",
            requestId,
            request.getRequestURI(),
            exception
        );

        ApiErrorResponse response = createResponse(
            "INTERNAL_ERROR",
            "Ocorreu um erro interno inesperado",
            null,
            request,
            requestId
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }

    private ApiErrorResponse createResponse(
        String error,
        String message,
        List<ApiErrorResponse.FieldErrorDetail> details,
        HttpServletRequest request,
        String requestId
    ) {
        return new ApiErrorResponse(
            error,
            message,
            details,
            Instant.now(),
            request.getRequestURI(),
            requestId
        );
    }
}