package com.raizes.nordeste.pedidos.infrastructure.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "LoginResponse",
    description =
        "Token JWT emitido após autenticação"
)
public record LoginResponseDTO(

    @Schema(
        description =
            "Token JWT utilizado nas rotas protegidas",
        example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    String token,

    @Schema(
        description =
            "Tipo de autenticação do token",
        example = "Bearer"
    )
    String tipo,

    @Schema(
        description =
            "Tempo de validade do token "
                + "em milissegundos",
        example = "86400000"
    )
    Long expiracaoEm
) {

    public LoginResponseDTO(
        String token,
        Long expiracaoEm
    ) {
        this(
            token,
            "Bearer",
            expiracaoEm
        );
    }
}