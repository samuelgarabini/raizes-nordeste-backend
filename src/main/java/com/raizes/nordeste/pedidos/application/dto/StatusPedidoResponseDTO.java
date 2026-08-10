package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.StatusPedido;

import java.util.UUID;

public record StatusPedidoResponseDTO(
    UUID pedidoId,
    StatusPedido statusAnterior,
    StatusPedido statusAtual
) {
}