package com.raizes.nordeste.pedidos.infrastructure.web;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CriarPedidoRequest(

    @NotNull(message = "clienteId é obrigatório")
    UUID clienteId,

    @NotNull(message = "unidadeId é obrigatória")
    UUID unidadeId,

    @NotNull(message = "canalPedido é obrigatório")
    CanalPedido canalPedido,

    @NotEmpty(message = "o pedido deve conter pelo menos um item")
    @Size(
        max = 50,
        message = "o pedido não pode conter mais de 50 itens diferentes"
    )
    List<@Valid ItemPedidoRequest> itens

) {
}