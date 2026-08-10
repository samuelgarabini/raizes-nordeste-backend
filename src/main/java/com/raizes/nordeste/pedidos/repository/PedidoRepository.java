package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.Pedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pedido FROM Pedido pedido WHERE pedido.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") UUID id);
}