package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.CanalPedido;

import java.util.List;
import java.util.UUID;

public record CriarPedidoCommand(
    UUID clienteId,
    UUID unidadeId,
    CanalPedido canalPedido,
    List<Item> itens
) {

    public record Item(
        Long produtoId,
        Integer quantidade
    ) {
    }
}