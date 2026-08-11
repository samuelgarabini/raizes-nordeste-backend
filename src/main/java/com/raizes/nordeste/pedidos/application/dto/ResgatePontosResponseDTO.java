package com.raizes.nordeste.pedidos.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(
    name = "ResgatePontosResponse",
    description = "Resultado do débito de pontos"
)
public record ResgatePontosResponseDTO(
    @Schema(
        description = "Identificador da movimentação",
        example =
            "20c4de2d-bb2e-41b4-a5ca-cb0fd18c2b7e"
    )
    UUID operacaoId,

    @Schema(
        description = "Identificador do cliente",
        example =
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    )
    UUID clienteId,

    @Schema(
        description = "Quantidade debitada",
        example = "10"
    )
    Integer pontosResgatados,

    @Schema(
        description = "Saldo antes do resgate",
        example = "55"
    )
    Integer saldoAnterior,

    @Schema(
        description = "Saldo depois do resgate",
        example = "45"
    )
    Integer saldoAtual,

    @Schema(
        description = "Data e hora do resgate",
        format = "date-time"
    )
    LocalDateTime resgatadoEm
) {
}
