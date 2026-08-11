package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.ConsentimentoFidelidade;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(
    name = "ConsentimentoFidelidadeResponse",
    description =
        "Estado atual do consentimento para participar do programa"
)
public record ConsentimentoFidelidadeResponseDTO(
    @Schema(
        description = "Identificador do cliente",
        example =
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    )
    UUID clienteId,

    @Schema(
        description =
            "Indica se a participação está autorizada",
        example = "true"
    )
    boolean concedido,

    @Schema(
        description = "Versão aceita do termo",
        example = "1.0"
    )
    String versaoTermo,

    @Schema(
        description = "Finalidade específica do tratamento",
        example = "PROGRAMA_FIDELIDADE"
    )
    String finalidade,

    @Schema(
        description = "Base legal registrada",
        example = "CONSENTIMENTO"
    )
    String baseLegal,

    @Schema(
        description = "Instante da concessão mais recente",
        nullable = true,
        format = "date-time"
    )
    OffsetDateTime concedidoEm,

    @Schema(
        description = "Instante da revogação mais recente",
        nullable = true,
        format = "date-time"
    )
    OffsetDateTime revogadoEm,

    @Schema(
        description = "Última atualização do registro",
        nullable = true,
        format = "date-time"
    )
    OffsetDateTime atualizadoEm
) {

    public static ConsentimentoFidelidadeResponseDTO de(
        ConsentimentoFidelidade consentimento
    ) {
        return new ConsentimentoFidelidadeResponseDTO(
            consentimento.getClienteId(),
            consentimento.isConcedido(),
            consentimento.getVersaoTermo(),
            consentimento.getFinalidade(),
            consentimento.getBaseLegal(),
            consentimento.getConcedidoEm(),
            consentimento.getRevogadoEm(),
            consentimento.getAtualizadoEm()
        );
    }

    public static ConsentimentoFidelidadeResponseDTO
        naoRegistrado(UUID clienteId) {

        return new ConsentimentoFidelidadeResponseDTO(
            clienteId,
            false,
            null,
            ConsentimentoFidelidade.FINALIDADE,
            ConsentimentoFidelidade.BASE_LEGAL,
            null,
            null,
            null
        );
    }
}
