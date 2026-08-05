package com.raizes.nordeste.pedidos.infrastructure.web;

import com.raizes.nordeste.pedidos.application.BuscarPedidoPorIdUseCase;
import com.raizes.nordeste.pedidos.application.CriarPedidoUseCase;
import com.raizes.nordeste.pedidos.domain.Pedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final CriarPedidoUseCase criarPedidoUseCase;
    private final BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase;

    public PedidoController(CriarPedidoUseCase criarPedidoUseCase, 
                            BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase) {
        this.criarPedidoUseCase = criarPedidoUseCase;
        this.buscarPedidoPorIdUseCase = buscarPedidoPorIdUseCase;
    }

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody CriarPedidoRequest request) {
        Pedido pedido = criarPedidoUseCase.executar(
            request.clienteId(),
            request.unidadeId(),
            request.canal(),
            request.valorTotal()
        );
        return ResponseEntity.ok(pedido);
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