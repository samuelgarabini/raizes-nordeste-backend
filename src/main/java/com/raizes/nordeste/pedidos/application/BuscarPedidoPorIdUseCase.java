package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BuscarPedidoPorIdUseCase {

    private final PedidoRepository pedidoRepository;

    public BuscarPedidoPorIdUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    // A anotação @Cacheable armazena o resultado no Redis sob a chave "pedidos::ID"
    @Cacheable(value = "pedidos", key = "#id")
    public Pedido executar(UUID id) {
        System.out.println(">>> BUSCANDO NO POSTGRESQL (Consulta sem cache)...");
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));
    }
}