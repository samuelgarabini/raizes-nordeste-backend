package com.raizes.nordeste.pedidos.infrastructure.security;

import com.raizes.nordeste.pedidos.infrastructure.security.dto.LoginRequestDTO;
import com.raizes.nordeste.pedidos.infrastructure.security.dto.LoginResponseDTO;
import com.raizes.nordeste.pedidos.presentation.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
    name = "Autenticação",
    description =
        "Autenticação de usuários e emissão de tokens JWT"
)
public class AuthController {

    private final AutenticacaoService autenticacaoService;

    public AuthController(
        AutenticacaoService autenticacaoService
    ) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuário",
        description =
            "Valida as credenciais de um usuário ativo "
                + "persistido no banco e retorna um token JWT. "
                + "Este endpoint é público e não exige token.",
        security = {},
        requestBody =
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                required = true,
                description =
                    "Nome de usuário e senha cadastrados",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        implementation =
                            LoginRequestDTO.class
                    ),
                    examples = @ExampleObject(
                        name = "Login de cliente",
                        value = """
                            {
                              "username": "cliente",
                              "password": "Senha@123"
                            }
                            """
                    )
                )
            )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description =
                "Usuário autenticado e token JWT emitido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        LoginResponseDTO.class
                ),
                examples = @ExampleObject(
                    name = "Token emitido",
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiJ9...",
                          "tipo": "Bearer",
                          "expiracaoEm": 86400000
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description =
                "Campos ausentes ou conteúdo inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                ),
                examples = @ExampleObject(
                    name = "Erro de validação",
                    value = """
                        {
                          "error": "VALIDATION_ERROR",
                          "message": "Erro de validação nos campos da requisição",
                          "details": [
                            {
                              "field": "username",
                              "issue": "O usuário é obrigatório"
                            }
                          ],
                          "timestamp": "2026-08-11T12:00:00Z",
                          "path": "/api/v1/auth/login",
                          "requestId": "b24d630d-4216-4c28-aeca-72e8d3ea54b9"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description =
                "Usuário ou senha inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                ),
                examples = @ExampleObject(
                    name = "Credenciais inválidas",
                    value = """
                        {
                          "error": "CREDENCIAIS_INVALIDAS",
                          "message": "Usuário ou senha inválidos",
                          "details": null,
                          "timestamp": "2026-08-11T12:00:00Z",
                          "path": "/api/v1/auth/login",
                          "requestId": "e5355451-7ebc-46a8-b175-0ce0acd27620"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<LoginResponseDTO> login(
        @RequestBody @Valid LoginRequestDTO request,
        HttpServletRequest httpRequest
    ) {
        LoginResponseDTO response =
            autenticacaoService.autenticar(
                request,
                httpRequest.getRemoteAddr()
            );

        return ResponseEntity.ok(response);
    }
}