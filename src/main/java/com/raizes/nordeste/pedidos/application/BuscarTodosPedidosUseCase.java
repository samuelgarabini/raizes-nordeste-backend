package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuscarTodosPedidosUseCase {

    private final PedidoRepository pedidoRepository;

    public BuscarTodosPedidosUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<Pedido> executar() {
        return pedidoRepository.findAll();
    }
}