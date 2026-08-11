package com.raizes.nordeste.pedidos.presentation;

import com.raizes.nordeste.pedidos.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(
    name = "LGPD - demonstração",
    description =
        "Endpoint demonstrativo que ainda não "
            + "realiza anonimização persistente"
)
@SecurityRequirement(
    name = OpenApiConfig.SECURITY_SCHEME_NAME
)
public class SystemMockController {

    @DeleteMapping("/lgpd/anonimizar")
    @Operation(
        summary =
            "Simular solicitação de anonimização",
        description =
            "Endpoint temporário e não funcional. "
                + "A resposta apenas demonstra o formato "
                + "pretendido e não altera CPF, e-mail "
                + "ou qualquer outro dado no banco. "
                + "Não deve ser utilizado como evidência "
                + "de conformidade com a LGPD.",
        deprecated = true
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description =
                "Resposta demonstrativa produzida",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object"
                ),
                examples = @ExampleObject(
                    name =
                        "Resposta exclusivamente demonstrativa",
                    value = """
                        {
                          "status": "SUCESSO",
                          "mensagem": "Os dados sensíveis do cliente a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11 foram anonimizados."
                        }
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
                )
            )
        )
    })
    public ResponseEntity<Map<String, String>>
        anonimizarDados(
            @Parameter(
                description =
                    "Identificador informado apenas "
                        + "para compor a mensagem. "
                        + "Nenhuma consulta ou alteração "
                        + "é realizada.",
                example =
                    "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
            )
            @RequestParam(required = false)
                String clienteId
        ) {

        String idAlvo =
            clienteId != null
                ? clienteId
                : "desconhecido";

        return ResponseEntity.ok(
            Map.of(
                "status",
                "SUCESSO",
                "mensagem",
                "Os dados sensíveis do cliente "
                    + idAlvo
                    + " foram anonimizados."
            )
        );
    }
}