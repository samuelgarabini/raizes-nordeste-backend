package com.raizes.nordeste.pedidos.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
    name = "ItemPedidoRequest",
    description =
        "Produto e quantidade solicitados"
)
public record ItemPedidoRequest(

    @Schema(
        description =
            "Identificador numérico do produto",
        example = "101",
        requiredMode =
            Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "produtoId é obrigatório")
    Long produtoId,

    @Schema(
        description =
            "Quantidade solicitada do produto",
        example = "2",
        minimum = "1",
        requiredMode =
            Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "quantidade é obrigatória")
    @Positive(
        message =
            "quantidade deve ser maior que zero"
    )
    Integer quantidade

) {
}