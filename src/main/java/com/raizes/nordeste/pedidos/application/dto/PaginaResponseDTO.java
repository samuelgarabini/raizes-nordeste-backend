package com.raizes.nordeste.pedidos.application.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaResponseDTO<T>(
    List<T> conteudo,
    int pagina,
    int tamanho,
    long totalElementos,
    int totalPaginas,
    boolean primeiraPagina,
    boolean ultimaPagina
) {

    public static <T> PaginaResponseDTO<T> de(
        Page<T> resultado
    ) {
        return new PaginaResponseDTO<>(
            resultado.getContent(),
            resultado.getNumber(),
            resultado.getSize(),
            resultado.getTotalElements(),
            resultado.getTotalPages(),
            resultado.isFirst(),
            resultado.isLast()
        );
    }
}