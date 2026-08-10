package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagamentoRepository
    extends JpaRepository<Pagamento, UUID> {

    Optional<Pagamento> findByPedidoId(
        UUID pedidoId
    );

    boolean existsByPedidoId(
        UUID pedidoId
    );
}