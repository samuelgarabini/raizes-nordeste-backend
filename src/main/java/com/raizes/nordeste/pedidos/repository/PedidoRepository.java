package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
}