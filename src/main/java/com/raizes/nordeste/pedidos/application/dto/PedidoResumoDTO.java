package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PedidoResumoDTO(
    UUID id,
    UUID clienteId,
    UUID unidadeId,
    CanalPedido canalPedido,
    BigDecimal valorTotal,
    StatusPedido status,
    OffsetDateTime dataHora
) {

    public static PedidoResumoDTO de(
        Pedido pedido
    ) {
        return new PedidoResumoDTO(
            pedido.getId(),
            pedido.getClienteId(),
            pedido.getUnidadeId(),
            pedido.getCanalPedido(),
            pedido.getValorTotal(),
            pedido.getStatus(),
            pedido.getDataHora()
        );
    }
}