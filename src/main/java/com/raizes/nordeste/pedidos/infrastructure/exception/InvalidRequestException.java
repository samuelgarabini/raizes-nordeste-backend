package com.raizes.nordeste.pedidos.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException
    extends ApiException {

    public InvalidRequestException(
        String errorCode,
        String message
    ) {
        super(
            errorCode,
            message,
            HttpStatus.BAD_REQUEST
        );
    }
}