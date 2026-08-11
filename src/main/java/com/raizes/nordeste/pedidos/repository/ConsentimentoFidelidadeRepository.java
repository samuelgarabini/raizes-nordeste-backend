package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.ConsentimentoFidelidade;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentimentoFidelidadeRepository
    extends JpaRepository<ConsentimentoFidelidade, UUID> {

    Optional<ConsentimentoFidelidade> findByClienteId(
        UUID clienteId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT consentimento
        FROM ConsentimentoFidelidade consentimento
        WHERE consentimento.clienteId = :clienteId
        """)
    Optional<ConsentimentoFidelidade>
        findByClienteIdForUpdate(
            @Param("clienteId") UUID clienteId
        );
}
