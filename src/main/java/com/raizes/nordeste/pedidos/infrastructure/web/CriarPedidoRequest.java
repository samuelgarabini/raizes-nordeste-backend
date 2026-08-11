package com.raizes.nordeste.pedidos.infrastructure.web;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(
    name = "CriarPedidoRequest",
    description =
        "Dados necessários para criar "
            + "um pedido multicanal"
)
public record CriarPedidoRequest(

    @Schema(
        description =
            "Identificador UUID do cliente",
        example =
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
        requiredMode =
            Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "clienteId é obrigatório")
    UUID clienteId,

    @Schema(
        description =
            "Identificador UUID da unidade",
        example =
            "550e8400-e29b-41d4-a716-446655440000",
        requiredMode =
            Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "unidadeId é obrigatória")
    UUID unidadeId,

    @Schema(
        description =
            "Canal em que o pedido foi originado",
        example = "APP",
        allowableValues = {
            "APP",
            "TOTEM",
            "BALCAO",
            "PICKUP",
            "WEB"
        },
        requiredMode =
            Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "canalPedido é obrigatório")
    CanalPedido canalPedido,

    @Schema(
        description =
            "Itens solicitados. Deve conter "
                + "entre 1 e 50 itens diferentes.",
        requiredMode =
            Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(
        message =
            "o pedido deve conter pelo menos um item"
    )
    @Size(
        max = 50,
        message =
            "o pedido não pode conter mais "
                + "de 50 itens diferentes"
    )
    List<@Valid ItemPedidoRequest> itens

) {
}