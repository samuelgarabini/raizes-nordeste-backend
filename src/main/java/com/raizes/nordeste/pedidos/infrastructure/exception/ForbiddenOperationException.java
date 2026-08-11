package com.raizes.nordeste.pedidos.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenOperationException
    extends ApiException {

    public ForbiddenOperationException(
        String errorCode,
        String message
    ) {
        super(errorCode, message, HttpStatus.FORBIDDEN);
    }
}
