package com.raizes.nordeste.pedidos.infrastructure.security.dto;

public record LoginResponseDTO(
    String token,
    String tipo,
    Long expiracaoEm
) {
    public LoginResponseDTO(String token, Long expiracaoEm) {
        this(token, "Bearer", expiracaoEm);
    }
}