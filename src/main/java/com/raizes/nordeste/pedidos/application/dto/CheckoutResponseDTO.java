package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.Pagamento;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPagamento;
import com.raizes.nordeste.pedidos.domain.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CheckoutResponseDTO(
    UUID pedidoId,
    StatusPedido status,
    BigDecimal valorTotal,
    UUID transacaoId,
    StatusPagamento statusPagamento,
    String gateway,
    String motivoRecusa,
    LocalDateTime processadoEm
) {

    public static CheckoutResponseDTO de(
        Pedido pedido,
        Pagamento pagamento
    ) {
        return new CheckoutResponseDTO(
            pedido.getId(),
            pedido.getStatus(),
            pedido.getValorTotal(),
            pagamento.getTransacaoId(),
            pagamento.getStatus(),
            pagamento.getGateway(),
            pagamento.getMotivoRecusa(),
            pagamento.getProcessadoEm()
        );
    }
}