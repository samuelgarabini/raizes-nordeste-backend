package com.raizes.nordeste.pedidos.domain;

import org.springframework.stereotype.Repository;

@Repository
public class PedidoRepository {

    public Pedido save(Pedido pedido) {
        return pedido;
    }
}