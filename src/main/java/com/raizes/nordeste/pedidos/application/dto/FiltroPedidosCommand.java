package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;

import java.util.UUID;

public record FiltroPedidosCommand(
    CanalPedido canalPedido,
    StatusPedido status,
    UUID unidadeId,
    int pagina,
    int tamanho
) {
}