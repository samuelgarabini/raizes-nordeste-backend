package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.StatusPagamento;

import java.util.UUID;

public record ProcessarCheckoutCommand(
    UUID pedidoId,
    String codigoPromocional,
    StatusPagamento resultadoPagamento
) {
}