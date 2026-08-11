package com.raizes.nordeste.pedidos.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
    name = "AtualizarConsentimentoFidelidadeRequest",
    description =
        "Decisão explícita do cliente sobre a participação no programa"
)
public record AtualizarConsentimentoFidelidadeRequest(
    @NotNull(
        message = "concedido é obrigatório"
    )
    @Schema(
        description =
            "true concede e false revoga o consentimento",
        example = "true"
    )
    Boolean concedido,

    @NotBlank(
        message = "versaoTermo é obrigatória"
    )
    @Size(
        max = 50,
        message =
            "versaoTermo deve possuir no máximo 50 caracteres"
    )
    @Schema(
        description = "Versão do termo apresentado",
        example = "1.0"
    )
    String versaoTermo
) {
}
