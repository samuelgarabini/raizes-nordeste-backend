package com.raizes.nordeste.pedidos.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class BusinessConflictException extends ApiException {

    public BusinessConflictException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.CONFLICT);
    }
}