package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CriarPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public CriarPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido executar(UUID clienteId, UUID unidadeId, CanalPedido canal, BigDecimal valorTotal) {
        Pedido pedido = new Pedido(UUID.randomUUID(), clienteId, unidadeId, canal, valorTotal, "CRIADO");
        return pedidoRepository.save(pedido);
    }
}