package com.raizes.nordeste.pedidos.presentation.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProdutoDTO(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    String categoria
) implements Serializable {}