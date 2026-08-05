package com.raizes.nordeste.pedidos.infrastructure.messaging;

import java.math.BigDecimal;
import java.io.Serializable;

public record PedidoCriadoEvent(
    String pedidoId,
    String clienteId,
    BigDecimal valorTotal
) implements Serializable {}