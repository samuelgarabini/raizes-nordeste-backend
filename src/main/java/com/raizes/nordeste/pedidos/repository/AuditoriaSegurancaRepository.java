package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.infrastructure.audit.AuditoriaSeguranca;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface AuditoriaSegurancaRepository
    extends Repository<AuditoriaSeguranca, UUID> {

    AuditoriaSeguranca save(
        AuditoriaSeguranca auditoria
    );
}