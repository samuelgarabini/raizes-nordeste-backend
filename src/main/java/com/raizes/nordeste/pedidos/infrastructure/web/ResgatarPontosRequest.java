package com.raizes.nordeste.pedidos.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
    name = "ResgatarPontosRequest",
    description = "Quantidade de pontos a debitar da carteira"
)
public record ResgatarPontosRequest(
    @NotNull(message = "pontos é obrigatório")
    @Positive(
        message = "pontos deve ser maior que zero"
    )
    @Schema(
        description = "Quantidade inteira de pontos",
        example = "10",
        minimum = "1"
    )
    Integer pontos
) {
}
