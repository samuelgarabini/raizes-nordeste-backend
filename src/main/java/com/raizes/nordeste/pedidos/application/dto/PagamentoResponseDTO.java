package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.Pagamento;
import com.raizes.nordeste.pedidos.domain.StatusPagamento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(
    name = "PagamentoResponse",
    description =
        "Pagamento mock persistido para o pedido"
)
public record PagamentoResponseDTO(

    @Schema(
        description =
            "Identificador fictício da transação",
        example =
            "02f0e1bc-d695-4b51-b00b-a0be208b645f"
    )
    UUID transacaoId,

    @Schema(
        description =
            "Resultado do pagamento",
        example = "APROVADO",
        allowableValues = {
            "APROVADO",
            "RECUSADO"
        }
    )
    StatusPagamento status,

    @Schema(
        description =
            "Valor processado pelo gateway mock",
        example = "58.30"
    )
    BigDecimal valor,

    @Schema(
        description =
            "Nome do gateway simulado",
        example = "MOCK_GATEWAY"
    )
    String gateway,

    @Schema(
        description =
            "Motivo da recusa, quando existente",
        example =
            "Pagamento recusado pelo gateway mock",
        nullable = true
    )
    String motivoRecusa,

    @Schema(
        description =
            "Data e hora de processamento",
        example = "2026-08-11T12:01:00",
        format = "date-time"
    )
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