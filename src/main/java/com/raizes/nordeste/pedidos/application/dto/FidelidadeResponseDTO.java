package com.raizes.nordeste.pedidos.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(
    name = "FidelidadeResponse",
    description = "Saldo, consentimento e histórico do cliente"
)
public record FidelidadeResponseDTO(
    @Schema(
        description = "Identificador do cliente",
        example =
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    )
    UUID clienteId,

    @Schema(
        description = "Saldo disponível em pontos",
        example = "55"
    )
    Integer saldoPontos,

    @Schema(
        description = "Última alteração do saldo",
        nullable = true,
        format = "date-time"
    )
    LocalDateTime ultimaAtualizacao,

    ConsentimentoFidelidadeResponseDTO consentimento,

    List<HistoricoPontosResponseDTO> historico
) {
}
