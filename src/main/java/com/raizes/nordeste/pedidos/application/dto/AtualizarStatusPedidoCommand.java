package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.StatusPedido;

import java.util.UUID;

public record AtualizarStatusPedidoCommand(
    UUID pedidoId,
    StatusPedido novoStatus
) {
}