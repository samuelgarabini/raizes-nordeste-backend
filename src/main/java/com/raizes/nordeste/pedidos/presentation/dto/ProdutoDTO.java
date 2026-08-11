package com.raizes.nordeste.pedidos.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(
    name = "ProdutoCardapio",
    description =
        "Produto disponível no cardápio "
            + "de uma unidade"
)
public record ProdutoDTO(

    @Schema(
        description =
            "Identificador numérico do produto",
        example = "101"
    )
    Long id,

    @Schema(
        description = "Nome do produto",
        example =
            "Cuscuz com Carne de Sol"
    )
    String nome,

    @Schema(
        description =
            "Descrição apresentada no cardápio",
        example =
            "Cuscuz nordestino acompanhado "
                + "de carne de sol."
    )
    String descricao,

    @Schema(
        description =
            "Preço atual do produto",
        example = "24.90"
    )
    BigDecimal preco,

    @Schema(
        description =
            "Categoria de exibição",
        example = "Pratos Principais"
    )
    String categoria
) implements Serializable {
}