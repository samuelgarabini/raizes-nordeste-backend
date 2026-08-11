package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
    name = "StatusPedidoResponse",
    description =
        "Resultado da transição operacional "
            + "do pedido"
)
public record StatusPedidoResponseDTO(

    @Schema(
        description =
            "Identificador UUID do pedido",
        example =
            "2f9210c7-798c-4f7d-9473-38cf70c508bb"
    )
    UUID pedidoId,

    @Schema(
        description =
            "Status anterior do pedido",
        example = "PAGO"
    )
    StatusPedido statusAnterior,

    @Schema(
        description =
            "Novo status do pedido",
        example = "EM_PREPARACAO"
    )
    StatusPedido statusAtual
) {
}