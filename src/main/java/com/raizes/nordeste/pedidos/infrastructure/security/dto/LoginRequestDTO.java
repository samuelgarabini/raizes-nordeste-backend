package com.raizes.nordeste.pedidos.infrastructure.security.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "O usuário/e-mail é obrigatório")
    String username,

    @NotBlank(message = "A senha é obrigatória")
    String password
) {}