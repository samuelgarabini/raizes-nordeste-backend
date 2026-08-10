package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.Pagamento;
import com.raizes.nordeste.pedidos.domain.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoResponseDTO(
    UUID transacaoId,
    StatusPagamento status,
    BigDecimal valor,
    String gateway,
    String motivoRecusa,
    LocalDateTime processadoEm
) {

    public static PagamentoResponseDTO de(
        Pagamento pagamento
    ) {
        return new PagamentoResponseDTO(
            pagamento.getTransacaoId(),
            pagamento.getStatus(),
            pagamento.getValor(),
            pagamento.getGateway(),
            pagamento.getMotivoRecusa(),
            pagamento.getProcessadoEm()
        );
    }
}