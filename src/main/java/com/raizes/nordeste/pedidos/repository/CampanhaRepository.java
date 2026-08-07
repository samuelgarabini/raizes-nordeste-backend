package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.Campanha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampanhaRepository extends JpaRepository<Campanha, UUID> {
    Optional<Campanha> findByCodigoPromocionalAndAtivoTrue(String codigoPromocional);
}