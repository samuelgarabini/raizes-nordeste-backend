package com.raizes.nordeste.pedidos.infrastructure.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequest(

    @NotNull(message = "produtoId é obrigatório")
    Long produtoId,

    @NotNull(message = "quantidade é obrigatória")
    @Positive(message = "quantidade deve ser maior que zero")
    Integer quantidade

) {
}