package com.raizes.nordeste.pedidos.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class CredenciaisInvalidasException
    extends ApiException {

    public CredenciaisInvalidasException() {
        super(
            "CREDENCIAIS_INVALIDAS",
            "Usuário ou senha inválidos",
            HttpStatus.UNAUTHORIZED
        );
    }
}