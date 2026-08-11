package com.raizes.nordeste.pedidos.infrastructure.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name = "LoginRequest",
    description =
        "Credenciais utilizadas para autenticação"
)
public record LoginRequestDTO(

    @Schema(
        description = "Nome de usuário cadastrado",
        example = "cliente",
        requiredMode =
            Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "O usuário é obrigatório")
    @Size(
        max = 100,
        message =
            "O usuário deve possuir no máximo "
                + "100 caracteres"
    )
    String username,

    @Schema(
        description = "Senha do usuário",
        example = "Senha@123",
        requiredMode =
            Schema.RequiredMode.REQUIRED,
        accessMode =
            Schema.AccessMode.WRITE_ONLY
    )
    @NotBlank(message = "A senha é obrigatória")
    @Size(
        max = 200,
        message =
            "A senha deve possuir no máximo "
                + "200 caracteres"
    )
    String password
) {
}