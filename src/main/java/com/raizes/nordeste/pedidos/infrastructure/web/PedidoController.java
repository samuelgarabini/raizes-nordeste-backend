package com.raizes.nordeste.pedidos.infrastructure.web;

import com.raizes.nordeste.pedidos.application.BuscarPedidoPorIdUseCase;
import com.raizes.nordeste.pedidos.application.BuscarTodosPedidosUseCase;
import com.raizes.nordeste.pedidos.application.CriarPedidoUseCase;
import com.raizes.nordeste.pedidos.application.ProcessarCheckoutUseCase;
import com.raizes.nordeste.pedidos.application.dto.CheckoutResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.CriarPedidoCommand;
import com.raizes.nordeste.pedidos.application.dto.FiltroPedidosCommand;
import com.raizes.nordeste.pedidos.application.dto.PaginaResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.PedidoCriadoDTO;
import com.raizes.nordeste.pedidos.application.dto.PedidoDetalheDTO;
import com.raizes.nordeste.pedidos.application.dto.PedidoResumoDTO;
import com.raizes.nordeste.pedidos.application.dto.ProcessarCheckoutCommand;
import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.StatusPagamento;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
public class PedidoController {

    private final CriarPedidoUseCase criarPedidoUseCase;

    private final BuscarPedidoPorIdUseCase
        buscarPedidoPorIdUseCase;

    private final BuscarTodosPedidosUseCase
        buscarTodosPedidosUseCase;

    private final ProcessarCheckoutUseCase
        processarCheckoutUseCase;

    public PedidoController(
        CriarPedidoUseCase criarPedidoUseCase,
        BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase,
        BuscarTodosPedidosUseCase buscarTodosPedidosUseCase,
        ProcessarCheckoutUseCase processarCheckoutUseCase
    ) {
        this.criarPedidoUseCase = criarPedidoUseCase;
        this.buscarPedidoPorIdUseCase =
            buscarPedidoPorIdUseCase;
        this.buscarTodosPedidosUseCase =
            buscarTodosPedidosUseCase;
        this.processarCheckoutUseCase =
            processarCheckoutUseCase;
    }

    @PostMapping
    public ResponseEntity<PedidoCriadoDTO> criarPedido(
        @RequestBody @Valid CriarPedidoRequest request
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
                    "/api/v1/pedidos/" + pedido.id()
                )
            )
            .body(pedido);
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<CheckoutResponseDTO>
        processarCheckout(
            @PathVariable UUID id,
            @RequestParam(required = false)
                String codigoPromocional,
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
            processarCheckoutUseCase.executar(command);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<
        PaginaResponseDTO<PedidoResumoDTO>
    > listarTodos(
        @RequestParam(required = false)
            CanalPedido canalPedido,
        @RequestParam(required = false)
            StatusPedido status,
        @RequestParam(required = false)
            UUID unidadeId,
        @RequestParam(defaultValue = "0")
            int pagina,
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

        PaginaResponseDTO<PedidoResumoDTO> response =
            buscarTodosPedidosUseCase.executar(filtros);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDetalheDTO> buscarPorId(
        @PathVariable UUID id
    ) {
        PedidoDetalheDTO response =
            buscarPedidoPorIdUseCase.executar(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(
            "API Raízes do Nordeste operacional!"
        );
    }
}