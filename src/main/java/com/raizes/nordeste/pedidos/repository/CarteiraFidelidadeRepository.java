package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.CarteiraFidelidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CarteiraFidelidadeRepository extends JpaRepository<CarteiraFidelidade, Long> {
    Optional<CarteiraFidelidade> findByClienteId(UUID clienteId);
}