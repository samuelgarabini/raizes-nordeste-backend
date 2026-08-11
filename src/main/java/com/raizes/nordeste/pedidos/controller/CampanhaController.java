package com.raizes.nordeste.pedidos.controller;

import com.raizes.nordeste.pedidos.domain.Campanha;
import com.raizes.nordeste.pedidos.infrastructure.config.OpenApiConfig;
import com.raizes.nordeste.pedidos.presentation.ApiErrorResponse;
import com.raizes.nordeste.pedidos.repository.CampanhaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/campanhas")
@Tag(
    name = "Campanhas promocionais",
    description =
        "Consulta das campanhas promocionais "
            + "cadastradas no sistema"
)
@SecurityRequirement(
    name = OpenApiConfig.SECURITY_SCHEME_NAME
)
public class CampanhaController {

    private final CampanhaRepository campanhaRepository;

    public CampanhaController(
        CampanhaRepository campanhaRepository
    ) {
        this.campanhaRepository = campanhaRepository;
    }

    @GetMapping
    @Operation(
        summary = "Listar campanhas",
        description =
            "Retorna todas as campanhas cadastradas, "
                + "incluindo código promocional, percentual "
                + "de desconto, valor mínimo, vigência "
                + "e situação. Exige autenticação JWT."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description =
                "Campanhas consultadas com sucesso",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(
                        implementation =
                            Campanha.class
                    )
                ),
                examples = @ExampleObject(
                    name = "Campanhas cadastradas",
                    value = """
                        [
                          {
                            "id": 1,
                            "nome": "Inauguração Raízes Nordeste",
                            "codigoPromocional": "BEMVINDO10",
                            "descontoPercentual": 10.00,
                            "valorMinimoPedido": 50.00,
                            "dataInicio": "2026-08-10T00:00:00",
                            "dataFim": "2026-09-09T00:00:00",
                            "ativo": true
                          },
                          {
                            "id": 2,
                            "nome": "Sextou com Frete Grátis",
                            "codigoPromocional": "SEXTOU",
                            "descontoPercentual": 5.00,
                            "valorMinimoPedido": 100.00,
                            "dataInicio": "2026-08-10T00:00:00",
                            "dataFim": "2026-08-17T00:00:00",
                            "ativo": true
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description =
                "Token JWT ausente, inválido ou expirado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                ),
                examples = @ExampleObject(
                    name = "Não autenticado",
                    value = """
                        {
                          "error": "NAO_AUTENTICADO",
                          "message": "É necessário fornecer uma autenticação válida",
                          "details": null,
                          "timestamp": "2026-08-11T12:00:00Z",
                          "path": "/api/campanhas",
                          "requestId": "af632e11-579e-4bbc-9f2e-f1b7f5a99cb1"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description =
                "Usuário autenticado sem perfil autorizado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                ),
                examples = @ExampleObject(
                    name = "Acesso negado",
                    value = """
                        {
                          "error": "ACESSO_NEGADO",
                          "message": "O usuário autenticado não possui permissão",
                          "details": null,
                          "timestamp": "2026-08-11T12:00:00Z",
                          "path": "/api/campanhas",
                          "requestId": "b869de8e-1a39-4a07-a111-d65839a5c64a"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<List<Campanha>>
        listarCampanhas() {

        return ResponseEntity.ok(
            campanhaRepository.findAll()
        );
    }
}