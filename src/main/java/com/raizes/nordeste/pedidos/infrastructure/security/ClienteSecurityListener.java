package com.raizes.nordeste.pedidos.infrastructure.security;

import com.raizes.nordeste.pedidos.domain.Cliente;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

@Component
public class ClienteSecurityListener {

    private final SensitiveDataFingerprintService
        fingerprintService;

    public ClienteSecurityListener(
        SensitiveDataFingerprintService
            fingerprintService
    ) {
        this.fingerprintService =
            fingerprintService;
    }

    @PrePersist
    @PreUpdate
    public void prepararDadosSensiveis(
        Cliente cliente
    ) {
        cliente.setCpfFingerprint(
            fingerprintService.fingerprintCpf(
                cliente.getCpf()
            )
        );
    }
}