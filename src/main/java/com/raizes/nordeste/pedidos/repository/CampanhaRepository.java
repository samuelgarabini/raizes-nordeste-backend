package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.Campanha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampanhaRepository extends JpaRepository<Campanha, Long> {
    Optional<Campanha> findByCodigoPromocionalAndAtivoTrue(String codigoPromocional);
}