package com.raizes.nordeste.pedidos.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "ApiError",
    description =
        "Formato padronizado das respostas de erro"
)
public class ApiErrorResponse {

    @Schema(
        description =
            "Código estável e legível do erro",
        example = "ESTOQUE_INSUFICIENTE"
    )
    private String error;

    @Schema(
        description =
            "Mensagem explicativa sem detalhes internos",
        example =
            "Estoque insuficiente para o produto: 101"
    )
    private String message;

    @Schema(
        description =
            "Erros específicos dos campos, "
                + "quando aplicável",
        nullable = true
    )
    private List<FieldErrorDetail> details;

    @Schema(
        description =
            "Instante em que o erro foi produzido",
        example = "2026-08-11T15:00:00Z",
        format = "date-time"
    )
    private Instant timestamp;

    @Schema(
        description =
            "Caminho HTTP que originou o erro",
        example = "/api/v1/pedidos"
    )
    private String path;

    @Schema(
        description =
            "Identificador utilizado para "
                + "correlacionar resposta e log",
        example =
            "a44a87f7-e297-491f-ab8a-bf72cc0d040a"
    )
    private String requestId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(
        name = "FieldErrorDetail",
        description =
            "Detalhe de validação de um campo"
    )
    public static class FieldErrorDetail {

        @Schema(
            description =
                "Nome do campo inválido",
            example = "canalPedido"
        )
        private String field;

        @Schema(
            description =
                "Motivo da rejeição do campo",
            example =
                "canalPedido é obrigatório"
        )
        private String issue;
    }
}