package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.entity.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponseDTO(
    Long produtoId,
    Integer quantidade,
    BigDecimal precoUnitario,
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