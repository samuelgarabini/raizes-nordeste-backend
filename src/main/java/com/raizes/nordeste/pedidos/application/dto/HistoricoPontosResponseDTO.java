package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.HistoricoPontos;
import com.raizes.nordeste.pedidos.domain.TipoOperacaoPontos;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(
    name = "HistoricoPontosResponse",
    description = "Movimentação da carteira de fidelidade"
)
public record HistoricoPontosResponseDTO(
    @Schema(
        description = "Identificador único da movimentação",
        example =
            "20c4de2d-bb2e-41b4-a5ca-cb0fd18c2b7e"
    )
    UUID operacaoId,

    @Schema(
        description =
            "Pedido que originou a movimentação, quando aplicável",
        nullable = true,
        example =
            "2f9210c7-798c-4f7d-9473-38cf70c508bb"
    )
    UUID pedidoId,

    @Schema(
        description = "Quantidade movimentada",
        example = "5"
    )
    Integer pontos,

    @Schema(
        description = "Natureza da movimentação",
        example = "CREDITO",
        allowableValues = {
            "CREDITO",
            "DEBITO",
            "ESTORNO"
        }
    )
    TipoOperacaoPontos tipoOperacao,

    @Schema(
        description = "Data e hora da movimentação",
        format = "date-time"
    )
    LocalDateTime dataOperacao
) {

    public static HistoricoPontosResponseDTO de(
        HistoricoPontos historico
    ) {
        return new HistoricoPontosResponseDTO(
            historico.getOperacaoId(),
            historico.getPedidoId(),
            historico.getPontosAlterados(),
            historico.getTipoOperacao(),
            historico.getDataOperacao()
        );
    }
}
