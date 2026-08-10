package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoRepository
    extends JpaRepository<Pedido, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT pedido
        FROM Pedido pedido
        WHERE pedido.id = :id
        """)
    Optional<Pedido> findByIdForUpdate(
        @Param("id") UUID id
    );

    @Query(
        value = """
            SELECT pedido
            FROM Pedido pedido
            WHERE (
                :canalPedido IS NULL
                OR pedido.canalPedido = :canalPedido
            )
            AND (
                :status IS NULL
                OR pedido.status = :status
            )
            AND (
                :unidadeId IS NULL
                OR pedido.unidadeId = :unidadeId
            )
            """,
        countQuery = """
            SELECT COUNT(pedido)
            FROM Pedido pedido
            WHERE (
                :canalPedido IS NULL
                OR pedido.canalPedido = :canalPedido
            )
            AND (
                :status IS NULL
                OR pedido.status = :status
            )
            AND (
                :unidadeId IS NULL
                OR pedido.unidadeId = :unidadeId
            )
            """
    )
    Page<Pedido> buscarComFiltros(
        @Param("canalPedido")
            CanalPedido canalPedido,
        @Param("status")
            StatusPedido status,
        @Param("unidadeId")
            UUID unidadeId,
        Pageable pageable
    );
}