package com.raizes.nordeste.pedidos.application.dto;

import java.util.UUID;

public record ResgatarPontosCommand(
    UUID clienteId,
    int pontos
) {
}
