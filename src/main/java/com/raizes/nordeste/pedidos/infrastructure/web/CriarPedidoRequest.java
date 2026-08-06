package com.raizes.nordeste.pedidos.infrastructure.web;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CriarPedidoRequest(
    @NotNull(message = "clienteId é obrigatório")
    UUID clienteId,

    @NotNull(message = "unidadeId é obrigatória")
    UUID unidadeId,

    @NotNull(message = "canal é obrigatório")
    CanalPedido canal,

    @NotNull(message = "valorTotal é obrigatório")
    @Positive(message = "valorTotal deve ser maior que zero")
    BigDecimal valorTotal
) {}