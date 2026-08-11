package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(
    name = "PedidoCriado",
    description =
        "Pedido persistido após validação "
            + "e reserva do estoque"
)
public record PedidoCriadoDTO(

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
        example = "APP",
        allowableValues = {
            "APP",
            "TOTEM",
            "BALCAO",
            "PICKUP",
            "WEB"
        }
    )
    CanalPedido canalPedido,

    @Schema(
        description =
            "Valor total calculado pelo servidor",
        example = "58.30"
    )
    BigDecimal valorTotal,

    @Schema(
        description =
            "Status inicial do pedido",
        example = "AGUARDANDO_PAGAMENTO"
    )
    StatusPedido status,

    @Schema(
        description =
            "Itens persistidos no pedido"
    )
    List<Item> itens
) {

    @Schema(
        name = "PedidoCriadoItem",
        description =
            "Item calculado e persistido no pedido"
    )
    public record Item(

        @Schema(
            description =
                "Identificador do produto",
            example = "101"
        )
        Long produtoId,

        @Schema(
            description =
                "Nome do produto no cardápio",
            example =
                "Cuscuz com Carne de Sol"
        )
        String nome,

        @Schema(
            description =
                "Quantidade solicitada",
            example = "2"
        )
        Integer quantidade,

        @Schema(
            description =
                "Preço unitário obtido do banco",
            example = "24.90"
        )
        BigDecimal precoUnitario,

        @Schema(
            description =
                "Subtotal calculado para o item",
            example = "49.80"
        )
        BigDecimal subtotal
    ) {
    }
}