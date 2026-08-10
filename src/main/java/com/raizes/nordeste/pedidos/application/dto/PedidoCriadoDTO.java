package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PedidoCriadoDTO(
    UUID id,
    UUID clienteId,
    UUID unidadeId,
    CanalPedido canalPedido,
    BigDecimal valorTotal,
    StatusPedido status,
    List<Item> itens
) {

    public record Item(
        Long produtoId,
        String nome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
    ) {
    }
}