package com.raizes.nordeste.pedidos.application.dto;

import java.util.UUID;

public record AtualizarConsentimentoFidelidadeCommand(
    UUID clienteId,
    boolean concedido,
    String versaoTermo
) {
}
