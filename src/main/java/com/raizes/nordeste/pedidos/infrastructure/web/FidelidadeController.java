package com.raizes.nordeste.pedidos.infrastructure.web;

import com.raizes.nordeste.pedidos.application.AtualizarConsentimentoFidelidadeUseCase;
import com.raizes.nordeste.pedidos.application.ConsultarFidelidadeUseCase;
import com.raizes.nordeste.pedidos.application.ResgatarPontosUseCase;
import com.raizes.nordeste.pedidos.application.dto.AtualizarConsentimentoFidelidadeCommand;
import com.raizes.nordeste.pedidos.application.dto.ConsentimentoFidelidadeResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.FidelidadeResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.ResgatarPontosCommand;
import com.raizes.nordeste.pedidos.application.dto.ResgatePontosResponseDTO;
import com.raizes.nordeste.pedidos.infrastructure.config.OpenApiConfig;
import com.raizes.nordeste.pedidos.presentation.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fidelidade")
@Tag(
    name = "Fidelidade",
    description =
        "Consentimento, saldo, histórico e resgate de pontos"
)
@SecurityRequirement(
    name = OpenApiConfig.SECURITY_SCHEME_NAME
)
@ApiResponses({
    @ApiResponse(
        responseCode = "401",
        description =
            "Token JWT ausente, inválido ou expirado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(
                implementation = ApiErrorResponse.class
            )
        )
    ),
    @ApiResponse(
        responseCode = "403",
        description =
            "Perfil sem permissão ou tentativa de acessar outro cliente",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(
                implementation = ApiErrorResponse.class
            )
        )
    )
})
public class FidelidadeController {

    private final ConsultarFidelidadeUseCase
        consultarFidelidadeUseCase;

    private final AtualizarConsentimentoFidelidadeUseCase
        atualizarConsentimentoUseCase;

    private final ResgatarPontosUseCase
        resgatarPontosUseCase;

    public FidelidadeController(
        ConsultarFidelidadeUseCase
            consultarFidelidadeUseCase,
        AtualizarConsentimentoFidelidadeUseCase
            atualizarConsentimentoUseCase,
        ResgatarPontosUseCase resgatarPontosUseCase
    ) {
        this.consultarFidelidadeUseCase =
            consultarFidelidadeUseCase;
        this.atualizarConsentimentoUseCase =
            atualizarConsentimentoUseCase;
        this.resgatarPontosUseCase =
            resgatarPontosUseCase;
    }

    @GetMapping("/{clienteId}")
    @Operation(
        summary = "Consultar fidelidade",
        description =
            "Retorna consentimento, saldo e histórico. "
                + "CLIENTE acessa apenas o próprio cadastro; "
                + "ADMIN, GERENTE e ATENDENTE podem consultar."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Dados de fidelidade encontrados",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        FidelidadeResponseDTO.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Identificador inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cliente não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = ApiErrorResponse.class
                )
            )
        )
    })
    public ResponseEntity<FidelidadeResponseDTO> consultar(
        @Parameter(
            description = "Identificador UUID do cliente",
            required = true,
            example =
                "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        @PathVariable UUID clienteId
    ) {
        return ResponseEntity.ok(
            consultarFidelidadeUseCase.executar(
                clienteId
            )
        );
    }

    @PutMapping("/{clienteId}/consentimento")
    @Operation(
        summary = "Atualizar consentimento",
        description =
            "Concede ou revoga a participação no programa. "
                + "Somente o próprio usuário CLIENTE pode "
                + "realizar esta operação. A revogação "
                + "preserva saldo e histórico, mas impede "
                + "novos créditos e resgates."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Consentimento atualizado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ConsentimentoFidelidadeResponseDTO.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Corpo ou versão do termo inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cliente não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = ApiErrorResponse.class
                )
            )
        )
    })
    public ResponseEntity<
        ConsentimentoFidelidadeResponseDTO
    > atualizarConsentimento(
        @PathVariable UUID clienteId,
        @RequestBody @Valid
            AtualizarConsentimentoFidelidadeRequest request
    ) {
        AtualizarConsentimentoFidelidadeCommand command =
            new AtualizarConsentimentoFidelidadeCommand(
                clienteId,
                request.concedido(),
                request.versaoTermo()
            );

        return ResponseEntity.ok(
            atualizarConsentimentoUseCase.executar(
                command
            )
        );
    }

    @PostMapping("/{clienteId}/resgates")
    @Operation(
        summary = "Resgatar pontos",
        description =
            "Debita pontos de forma transacional. Exige "
                + "consentimento ativo, saldo suficiente "
                + "e acesso do próprio usuário CLIENTE."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Pontos resgatados",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ResgatePontosResponseDTO.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Quantidade de pontos inválida",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cliente não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description =
                "Consentimento ausente/revogado ou saldo insuficiente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = ApiErrorResponse.class
                )
            )
        )
    })
    public ResponseEntity<ResgatePontosResponseDTO>
        resgatar(
            @PathVariable UUID clienteId,
            @RequestBody @Valid
                ResgatarPontosRequest request
        ) {

        ResgatarPontosCommand command =
            new ResgatarPontosCommand(
                clienteId,
                request.pontos()
            );

        return ResponseEntity.ok(
            resgatarPontosUseCase.executar(command)
        );
    }
}
