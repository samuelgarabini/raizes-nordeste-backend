package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.CarteiraFidelidade;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarteiraFidelidadeRepository
    extends JpaRepository<CarteiraFidelidade, Long> {

    Optional<CarteiraFidelidade> findByClienteId(
        UUID clienteId
    );

    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Query(
        value = """
            INSERT INTO carteiras_fidelidade (
                cliente_id,
                pontos_acumulados,
                ultima_atualizacao
            )
            VALUES (
                :clienteId,
                0,
                CURRENT_TIMESTAMP
            )
            ON CONFLICT (cliente_id) DO NOTHING
            """,
        nativeQuery = true
    )
    int criarSeAusente(
        @Param("clienteId") UUID clienteId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT carteira
        FROM CarteiraFidelidade carteira
        WHERE carteira.clienteId = :clienteId
        """)
    Optional<CarteiraFidelidade>
        findByClienteIdForUpdate(
            @Param("clienteId") UUID clienteId
        );
}