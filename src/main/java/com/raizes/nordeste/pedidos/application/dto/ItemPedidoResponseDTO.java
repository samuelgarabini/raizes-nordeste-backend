package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.entity.ItemPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
    name = "ItemPedidoResponse",
    description =
        "Item persistido de um pedido"
)
public record ItemPedidoResponseDTO(

    @Schema(
        description =
            "Identificador do produto",
        example = "101"
    )
    Long produtoId,

    @Schema(
        description =
            "Quantidade adquirida",
        example = "2"
    )
    Integer quantidade,

    @Schema(
        description =
            "Preço unitário preservado no pedido",
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

    public static ItemPedidoResponseDTO de(
        ItemPedido item
    ) {
        return new ItemPedidoResponseDTO(
            item.getProdutoId(),
            item.getQuantidade(),
            item.getPrecoUnitario(),
            item.getSubtotal()
        );
    }
}