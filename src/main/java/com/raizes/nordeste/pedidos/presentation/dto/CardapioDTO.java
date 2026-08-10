package com.raizes.nordeste.pedidos.presentation.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record CardapioDTO(
    UUID unidadeId,
    List<ProdutoDTO> itens
) implements Serializable {
}