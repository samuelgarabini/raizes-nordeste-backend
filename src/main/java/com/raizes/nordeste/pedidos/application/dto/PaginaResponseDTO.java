package com.raizes.nordeste.pedidos.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(
    description =
        "Resposta paginada de uma consulta"
)
public record PaginaResponseDTO<T>(

    @Schema(
        description =
            "Registros da página atual"
    )
    List<T> conteudo,

    @Schema(
        description =
            "Número da página atual, iniciado em zero",
        example = "0"
    )
    int pagina,

    @Schema(
        description =
            "Quantidade máxima de registros na página",
        example = "20"
    )
    int tamanho,

    @Schema(
        description =
            "Quantidade total de registros encontrados",
        example = "1"
    )
    long totalElementos,

    @Schema(
        description =
            "Quantidade total de páginas",
        example = "1"
    )
    int totalPaginas,

    @Schema(
        description =
            "Indica se esta é a primeira página",
        example = "true"
    )
    boolean primeiraPagina,

    @Schema(
        description =
            "Indica se esta é a última página",
        example = "true"
    )
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