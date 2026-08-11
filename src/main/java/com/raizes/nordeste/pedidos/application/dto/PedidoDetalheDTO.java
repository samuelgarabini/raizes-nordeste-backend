package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.Pagamento;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import com.raizes.nordeste.pedidos.domain.entity.ItemPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(
    name = "PedidoDetalhe",
    description =
        "Dados completos do pedido, seus itens "
            + "e eventual pagamento"
)
public record PedidoDetalheDTO(

    @Schema(
        description =
            "Identificador UUID do pedido",
        example =
            "2f9210c7-798c-4f7d-9473-38cf70c508bb"
    )
    UUID id,

    @Schema(
        description =
            "Identificador UUID do cliente",
        example =
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    )
    UUID clienteId,

    @Schema(
        description =
            "Identificador UUID da unidade",
        example =
            "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID unidadeId,

    @Schema(
        description =
            "Canal de origem do pedido",
        example = "APP"
    )
    CanalPedido canalPedido,

    @Schema(
        description =
            "Valor total atual do pedido",
        example = "58.30"
    )
    BigDecimal valorTotal,

    @Schema(
        description =
            "Status atual do pedido",
        example = "PAGO"
    )
    StatusPedido status,

    @Schema(
        description =
            "Data e hora de criação do pedido",
        example =
            "2026-08-11T12:00:00-03:00",
        format = "date-time"
    )
    OffsetDateTime dataHora,

    @Schema(
        description =
            "Itens persistidos no pedido"
    )
    List<ItemPedidoResponseDTO> itens,

    @Schema(
        description =
            "Pagamento associado ao pedido. "
                + "É nulo antes do checkout.",
        nullable = true
    )
    PagamentoResponseDTO pagamento
) {

    public PedidoDetalheDTO {
        itens = itens == null
            ? List.of()
            : List.copyOf(itens);
    }

    public static PedidoDetalheDTO de(
        Pedido pedido,
        List<ItemPedido> itens,
        Pagamento pagamento
    ) {
        List<ItemPedidoResponseDTO> itensResponse =
            itens.stream()
                .map(ItemPedidoResponseDTO::de)
                .toList();

        PagamentoResponseDTO pagamentoResponse =
            pagamento == null
                ? null
                : PagamentoResponseDTO.de(
                    pagamento
                );

        return new PedidoDetalheDTO(
            pedido.getId(),
            pedido.getClienteId(),
            pedido.getUnidadeId(),
            pedido.getCanalPedido(),
            pedido.getValorTotal(),
            pedido.getStatus(),
            pedido.getDataHora(),
            itensResponse,
            pagamentoResponse
        );
    }
}