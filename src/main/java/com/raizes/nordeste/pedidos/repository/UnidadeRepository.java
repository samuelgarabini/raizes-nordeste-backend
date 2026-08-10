package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.entity.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, UUID> {

    boolean existsByIdAndAtivaTrue(UUID id);
}