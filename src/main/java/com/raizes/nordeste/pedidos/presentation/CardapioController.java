package com.raizes.nordeste.pedidos.presentation;

import com.raizes.nordeste.pedidos.application.CardapioService;
import com.raizes.nordeste.pedidos.presentation.dto.CardapioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/unidades")
@RequiredArgsConstructor
@Tag(
    name = "Cardápio",
    description =
        "Consulta pública do cardápio disponível "
            + "em cada unidade"
)
public class CardapioController {

    private final CardapioService cardapioService;

    @GetMapping("/{unidadeId}/cardapio")
    @Operation(
        summary = "Consultar cardápio por unidade",
        description =
            "Retorna somente os produtos disponíveis "
                + "vinculados à unidade informada. "
                + "Quando não existem produtos associados, "
                + "a resposta contém uma lista vazia.",
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description =
                "Cardápio consultado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        CardapioDTO.class
                ),
                examples = @ExampleObject(
                    name = "Cardápio de Recife",
                    value = """
                        {
                          "unidadeId": "550e8400-e29b-41d4-a716-446655440000",
                          "itens": [
                            {
                              "id": 101,
                              "nome": "Cuscuz com Carne de Sol",
                              "descricao": "Cuscuz nordestino acompanhado de carne de sol.",
                              "preco": 24.90,
                              "categoria": "Pratos Principais"
                            },
                            {
                              "id": 104,
                              "nome": "Suco de Caju",
                              "descricao": "Suco natural de caju.",
                              "preco": 8.50,
                              "categoria": "Bebidas"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description =
                "Identificador da unidade em formato inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                ),
                examples = @ExampleObject(
                    name = "UUID inválido",
                    value = """
                        {
                          "error": "INVALID_REQUEST",
                          "message": "O conteúdo ou os parâmetros da requisição são inválidos",
                          "details": null,
                          "timestamp": "2026-08-11T12:00:00Z",
                          "path": "/api/v1/unidades/valor-invalido/cardapio",
                          "requestId": "2221a575-6fcf-4a71-8d80-92bf275523fc"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<CardapioDTO> getCardapio(
        @Parameter(
            description =
                "Identificador UUID da unidade",
            required = true,
            example =
                "550e8400-e29b-41d4-a716-446655440000"
        )
        @PathVariable UUID unidadeId
    ) {
        return ResponseEntity.ok(
            cardapioService.buscarCardapioPorUnidade(
                unidadeId
            )
        );
    }
}