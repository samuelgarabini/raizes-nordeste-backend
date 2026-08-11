package com.raizes.nordeste.pedidos.infrastructure.web;

import com.raizes.nordeste.pedidos.application.AtualizarStatusPedidoUseCase;
import com.raizes.nordeste.pedidos.application.BuscarPedidoPorIdUseCase;
import com.raizes.nordeste.pedidos.application.BuscarTodosPedidosUseCase;
import com.raizes.nordeste.pedidos.application.CriarPedidoUseCase;
import com.raizes.nordeste.pedidos.application.ProcessarCheckoutUseCase;
import com.raizes.nordeste.pedidos.application.dto.AtualizarStatusPedidoCommand;
import com.raizes.nordeste.pedidos.application.dto.CheckoutResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.CriarPedidoCommand;
import com.raizes.nordeste.pedidos.application.dto.FiltroPedidosCommand;
import com.raizes.nordeste.pedidos.application.dto.PaginaResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.PedidoCriadoDTO;
import com.raizes.nordeste.pedidos.application.dto.PedidoDetalheDTO;
import com.raizes.nordeste.pedidos.application.dto.PedidoResumoDTO;
import com.raizes.nordeste.pedidos.application.dto.ProcessarCheckoutCommand;
import com.raizes.nordeste.pedidos.application.dto.StatusPedidoResponseDTO;
import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.StatusPagamento;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import com.raizes.nordeste.pedidos.infrastructure.config.OpenApiConfig;
import com.raizes.nordeste.pedidos.presentation.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(
    name = "Pedidos",
    description =
        "Criação, consulta, checkout e ciclo "
            + "operacional dos pedidos multicanal"
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
                implementation =
                    ApiErrorResponse.class
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
            )
        )
    )
})
public class PedidoController {

    private final CriarPedidoUseCase
        criarPedidoUseCase;

    private final BuscarPedidoPorIdUseCase
        buscarPedidoPorIdUseCase;

    private final BuscarTodosPedidosUseCase
        buscarTodosPedidosUseCase;

    private final ProcessarCheckoutUseCase
        processarCheckoutUseCase;

    private final AtualizarStatusPedidoUseCase
        atualizarStatusPedidoUseCase;

    public PedidoController(
        CriarPedidoUseCase criarPedidoUseCase,
        BuscarPedidoPorIdUseCase
            buscarPedidoPorIdUseCase,
        BuscarTodosPedidosUseCase
            buscarTodosPedidosUseCase,
        ProcessarCheckoutUseCase
            processarCheckoutUseCase,
        AtualizarStatusPedidoUseCase
            atualizarStatusPedidoUseCase
    ) {
        this.criarPedidoUseCase =
            criarPedidoUseCase;
        this.buscarPedidoPorIdUseCase =
            buscarPedidoPorIdUseCase;
        this.buscarTodosPedidosUseCase =
            buscarTodosPedidosUseCase;
        this.processarCheckoutUseCase =
            processarCheckoutUseCase;
        this.atualizarStatusPedidoUseCase =
            atualizarStatusPedidoUseCase;
    }

    @PostMapping
    @Operation(
        summary = "Criar pedido",
        description =
            "Valida cliente, unidade, canal e itens; "
                + "calcula os valores no servidor, "
                + "reserva o estoque e persiste o pedido. "
                + "Perfis permitidos: ADMIN, GERENTE, "
                + "ATENDENTE e CLIENTE."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description =
                "Pedido criado e estoque reservado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        PedidoCriadoDTO.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description =
                "Campos ausentes, quantidade inválida "
                    + "ou conteúdo malformado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description =
                "Cliente, unidade ativa ou produto "
                    + "disponível não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description =
                "Estoque insuficiente ou produto "
                    + "repetido no pedido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        )
    })
    public ResponseEntity<PedidoCriadoDTO> criarPedido(
        @RequestBody @Valid
            CriarPedidoRequest request
    ) {
        CriarPedidoCommand command =
            new CriarPedidoCommand(
                request.clienteId(),
                request.unidadeId(),
                request.canalPedido(),
                request.itens()
                    .stream()
                    .map(item ->
                        new CriarPedidoCommand.Item(
                            item.produtoId(),
                            item.quantidade()
                        )
                    )
                    .toList()
            );

        PedidoCriadoDTO pedido =
            criarPedidoUseCase.executar(command);

        return ResponseEntity
            .created(
                URI.create(
                    "/api/v1/pedidos/"
                        + pedido.id()
                )
            )
            .body(pedido);
    }

    @PostMapping("/{id}/checkout")
    @Operation(
        summary = "Processar checkout",
        description =
            "Aplica opcionalmente um código promocional, "
                + "registra o pagamento mock e atualiza "
                + "pedido, estoque, fidelidade e auditoria. "
                + "Um pagamento recusado é um resultado "
                + "de negócio previsto e também retorna 200."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description =
                "Pagamento mock aprovado ou recusado "
                    + "e resultado persistido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        CheckoutResponseDTO.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description =
                "UUID, enum ou parâmetros inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pedido não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description =
                "Checkout não permitido, pagamento "
                    + "já processado, cupom inválido "
                    + "ou valor mínimo não atingido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        )
    })
    public ResponseEntity<CheckoutResponseDTO>
        processarCheckout(
            @Parameter(
                description =
                    "Identificador UUID do pedido",
                required = true,
                example =
                    "2f9210c7-798c-4f7d-9473-38cf70c508bb"
            )
            @PathVariable UUID id,

            @Parameter(
                description =
                    "Código promocional opcional",
                example = "BEMVINDO10"
            )
            @RequestParam(required = false)
                String codigoPromocional,

            @Parameter(
                description =
                    "Resultado devolvido pelo "
                        + "gateway de pagamento mock",
                schema = @Schema(
                    allowableValues = {
                        "APROVADO",
                        "RECUSADO"
                    },
                    defaultValue = "APROVADO"
                )
            )
            @RequestParam(defaultValue = "APROVADO")
                StatusPagamento resultadoPagamento
        ) {

        ProcessarCheckoutCommand command =
            new ProcessarCheckoutCommand(
                id,
                codigoPromocional,
                resultadoPagamento
            );

        CheckoutResponseDTO response =
            processarCheckoutUseCase.executar(
                command
            );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary =
            "Atualizar status operacional do pedido",
        description =
            "Avança o pedido pela sequência "
                + "PAGO → EM_PREPARACAO → PRONTO "
                + "→ ENTREGUE. Perfis permitidos: "
                + "ADMIN, GERENTE e ATENDENTE."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description =
                "Status atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        StatusPedidoResponseDTO.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description =
                "Novo status ausente ou parâmetro "
                    + "em formato inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pedido não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description =
                "Transição de status não permitida",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        )
    })
    public ResponseEntity<StatusPedidoResponseDTO>
        atualizarStatus(
            @Parameter(
                description =
                    "Identificador UUID do pedido",
                required = true,
                example =
                    "2f9210c7-798c-4f7d-9473-38cf70c508bb"
            )
            @PathVariable UUID id,

            @Parameter(
                description =
                    "Próximo status operacional",
                required = true,
                schema = @Schema(
                    allowableValues = {
                        "EM_PREPARACAO",
                        "PRONTO",
                        "ENTREGUE"
                    },
                    example = "EM_PREPARACAO"
                )
            )
            @RequestParam(required = false)
                StatusPedido novoStatus
        ) {

        AtualizarStatusPedidoCommand command =
            new AtualizarStatusPedidoCommand(
                id,
                novoStatus
            );

        StatusPedidoResponseDTO response =
            atualizarStatusPedidoUseCase
                .executar(command);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
        summary = "Listar pedidos",
        description =
            "Retorna pedidos de forma paginada, "
                + "ordenados do mais recente para "
                + "o mais antigo, com filtros opcionais "
                + "por canal, status e unidade."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description =
                "Página de pedidos consultada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        PaginaResponseDTO.class
                ),
                examples = @ExampleObject(
                    name =
                        "Página filtrada por canal",
                    value = """
                        {
                          "conteudo": [
                            {
                              "id": "2f9210c7-798c-4f7d-9473-38cf70c508bb",
                              "clienteId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                              "unidadeId": "550e8400-e29b-41d4-a716-446655440000",
                              "canalPedido": "TOTEM",
                              "valorTotal": 58.30,
                              "status": "PAGO",
                              "dataHora": "2026-08-11T12:00:00-03:00"
                            }
                          ],
                          "pagina": 0,
                          "tamanho": 20,
                          "totalElementos": 1,
                          "totalPaginas": 1,
                          "primeiraPagina": true,
                          "ultimaPagina": true
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description =
                "Filtro, enum ou configuração "
                    + "de paginação inválida",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        )
    })
    public ResponseEntity<
        PaginaResponseDTO<PedidoResumoDTO>
    > listarTodos(
        @Parameter(
            description =
                "Canal de origem do pedido",
            schema = @Schema(
                allowableValues = {
                    "APP",
                    "TOTEM",
                    "BALCAO",
                    "PICKUP",
                    "WEB"
                },
                example = "TOTEM"
            )
        )
        @RequestParam(required = false)
            CanalPedido canalPedido,

        @Parameter(
            description =
                "Status atual do pedido",
            schema = @Schema(
                allowableValues = {
                    "AGUARDANDO_PAGAMENTO",
                    "PAGO",
                    "PAGAMENTO_RECUSADO",
                    "EM_PREPARACAO",
                    "PRONTO",
                    "ENTREGUE",
                    "CANCELADO"
                },
                example = "PAGO"
            )
        )
        @RequestParam(required = false)
            StatusPedido status,

        @Parameter(
            description =
                "Identificador UUID da unidade",
            example =
                "550e8400-e29b-41d4-a716-446655440000"
        )
        @RequestParam(required = false)
            UUID unidadeId,

        @Parameter(
            description =
                "Número da página, iniciado em zero",
            schema = @Schema(
                type = "integer",
                minimum = "0",
                defaultValue = "0",
                example = "0"
            )
        )
        @RequestParam(defaultValue = "0")
            int pagina,

        @Parameter(
            description =
                "Quantidade de registros por página, "
                    + "entre 1 e 100",
            schema = @Schema(
                type = "integer",
                minimum = "1",
                maximum = "100",
                defaultValue = "20",
                example = "20"
            )
        )
        @RequestParam(defaultValue = "20")
            int tamanho
    ) {
        FiltroPedidosCommand filtros =
            new FiltroPedidosCommand(
                canalPedido,
                status,
                unidadeId,
                pagina,
                tamanho
            );

        PaginaResponseDTO<PedidoResumoDTO>
            response =
                buscarTodosPedidosUseCase
                    .executar(filtros);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Consultar detalhes do pedido",
        description =
            "Retorna dados do pedido, itens "
                + "persistidos e pagamento, quando "
                + "o checkout já foi processado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Pedido encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        PedidoDetalheDTO.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description =
                "Identificador em formato inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pedido não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation =
                        ApiErrorResponse.class
                )
            )
        )
    })
    public ResponseEntity<PedidoDetalheDTO>
        buscarPorId(
            @Parameter(
                description =
                    "Identificador UUID do pedido",
                required = true,
                example =
                    "2f9210c7-798c-4f7d-9473-38cf70c508bb"
            )
            @PathVariable UUID id
        ) {

        PedidoDetalheDTO response =
            buscarPedidoPorIdUseCase.executar(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Verificar disponibilidade do controller",
        description =
            "Retorna uma confirmação textual simples. "
                + "Esta rota não substitui probes de "
                + "saúde de todas as dependências."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Controller operacional",
        content = @Content(
            mediaType = "text/plain",
            schema = @Schema(
                type = "string",
                example =
                    "API Raízes do Nordeste operacional!"
            )
        )
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(
            "API Raízes do Nordeste operacional!"
        );
    }
}