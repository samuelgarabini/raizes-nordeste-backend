package com.raizes.nordeste.pedidos.infrastructure.web;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import java.math.BigDecimal;
import java.util.UUID;

public record CriarPedidoRequest(
    UUID clienteId,
    UUID unidadeId,
    CanalPedido canal,
    BigDecimal valorTotal
) {}