package com.raizes.nordeste.pedidos.presentation.dto;

import java.io.Serializable;
import java.util.List;

public record CardapioDTO(
    Long unidadeId,
    List<ProdutoDTO> itens
) implements Serializable {}