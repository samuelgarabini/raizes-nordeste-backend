package com.raizes.nordeste.pedidos.infrastructure.web;

import com.raizes.nordeste.pedidos.application.BuscarPedidoPorIdUseCase;
import com.raizes.nordeste.pedidos.application.BuscarTodosPedidosUseCase;
import com.raizes.nordeste.pedidos.application.CriarPedidoUseCase;
import com.raizes.nordeste.pedidos.domain.Pedido;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final CriarPedidoUseCase criarPedidoUseCase;
    private final BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase;
    private final BuscarTodosPedidosUseCase buscarTodosPedidosUseCase;

    public PedidoController(CriarPedidoUseCase criarPedidoUseCase, 
                            BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase,
                            BuscarTodosPedidosUseCase buscarTodosPedidosUseCase) {
        this.criarPedidoUseCase = criarPedidoUseCase;
        this.buscarPedidoPorIdUseCase = buscarPedidoPorIdUseCase;
        this.buscarTodosPedidosUseCase = buscarTodosPedidosUseCase;
    }

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody @Valid CriarPedidoRequest request) {
        Pedido pedido = criarPedidoUseCase.executar(
            request.clienteId(),
            request.unidadeId(),
            request.canal(),
            request.valorTotal()
        );
        return ResponseEntity.ok(pedido);
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos() {
        List<Pedido> pedidos = buscarTodosPedidosUseCase.executar();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable UUID id) {
        Pedido pedido = buscarPedidoPorIdUseCase.executar(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API Raízes do Nordeste operacional!");
    }
}