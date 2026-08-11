package com.raizes.nordeste.pedidos.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Schema(
    name = "Cardapio",
    description =
        "Cardápio público de uma unidade"
)
public record CardapioDTO(

    @Schema(
        description =
            "Identificador UUID da unidade",
        example =
            "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID unidadeId,

    @Schema(
        description =
            "Produtos disponíveis na unidade"
    )
    List<ProdutoDTO> itens
) implements Serializable {
}