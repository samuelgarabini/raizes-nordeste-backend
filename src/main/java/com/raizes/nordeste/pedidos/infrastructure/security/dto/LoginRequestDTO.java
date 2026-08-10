package com.raizes.nordeste.pedidos.infrastructure.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
    @NotBlank(message = "O usuário é obrigatório")
    @Size(
        max = 100,
        message = "O usuário deve possuir no máximo 100 caracteres"
    )
    String username,

    @NotBlank(message = "A senha é obrigatória")
    @Size(
        max = 200,
        message = "A senha deve possuir no máximo 200 caracteres"
    )
    String password
) {
}