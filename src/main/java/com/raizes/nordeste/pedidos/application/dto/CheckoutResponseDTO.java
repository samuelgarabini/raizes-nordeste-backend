package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.Pagamento;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPagamento;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(
    name = "CheckoutResponse",
    description =
        "Resultado persistido do pagamento mock"
)
public record CheckoutResponseDTO(

    @Schema(
        description =
            "Identificador UUID do pedido",
        example =
            "2f9210c7-798c-4f7d-9473-38cf70c508bb"
    )
    UUID pedidoId,

    @Schema(
        description =
            "Status do pedido após o checkout",
        example = "PAGO",
        allowableValues = {
            "PAGO",
            "PAGAMENTO_RECUSADO"
        }
    )
    StatusPedido status,

    @Schema(
        description =
            "Valor final após eventual desconto",
        example = "52.47"
    )
    BigDecimal valorTotal,

    @Schema(
        description =
            "Identificador fictício da transação",
        example =
            "02f0e1bc-d695-4b51-b00b-a0be208b645f"
    )
    UUID transacaoId,

    @Schema(
        description =
            "Resultado do pagamento mock",
        example = "APROVADO",
        allowableValues = {
            "APROVADO",
            "RECUSADO"
        }
    )
    StatusPagamento statusPagamento,

    @Schema(
        description =
            "Nome do gateway simulado",
        example = "MOCK_GATEWAY"
    )
    String gateway,

    @Schema(
        description =
            "Motivo da recusa, preenchido somente "
                + "quando o pagamento é recusado",
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