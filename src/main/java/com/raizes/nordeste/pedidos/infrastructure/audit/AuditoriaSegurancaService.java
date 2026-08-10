package com.raizes.nordeste.pedidos.infrastructure.audit;

import com.raizes.nordeste.pedidos.infrastructure.security.SensitiveDataFingerprintService;
import com.raizes.nordeste.pedidos.repository.AuditoriaSegurancaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AuditoriaSegurancaService {

    private static final String ACTOR_CONTEXT =
        "AUDIT_ACTOR";

    private static final String IP_CONTEXT =
        "AUDIT_IP";

    private final AuditoriaSegurancaRepository
        auditoriaRepository;

    private final SensitiveDataFingerprintService
        fingerprintService;

    public AuditoriaSegurancaService(
        AuditoriaSegurancaRepository
            auditoriaRepository,
        SensitiveDataFingerprintService
            fingerprintService
    ) {
        this.auditoriaRepository =
            auditoriaRepository;
        this.fingerprintService =
            fingerprintService;
    }

    @Transactional(
        propagation = Propagation.REQUIRES_NEW
    )
    public AuditoriaSeguranca registrar(
        RegistrarAuditoriaCommand command
    ) {
        validarCommand(command);

        AuditoriaSeguranca auditoria =
            AuditoriaSeguranca.builder()
                .evento(command.evento())
                .resultado(command.resultado())
                .atorFingerprint(
                    fingerprintService.fingerprint(
                        ACTOR_CONTEXT,
                        command.ator()
                    )
                )
                .perfil(command.perfil())
                .recurso(
                    normalizarCampo(
                        command.recurso(),
                        50,
                        "recurso"
                    )
                )
                .recursoId(
                    normalizarCampo(
                        command.recursoId(),
                        100,
                        "recursoId"
                    )
                )
                .ipFingerprint(
                    fingerprintService.fingerprint(
                        IP_CONTEXT,
                        command.enderecoIp()
                    )
                )
                .codigoErro(
                    normalizarCampo(
                        command.codigoErro(),
                        100,
                        "codigoErro"
                    )
                )
                .build();

        return auditoriaRepository.save(auditoria);
    }

    private void validarCommand(
        RegistrarAuditoriaCommand command
    ) {
        Objects.requireNonNull(
            command,
            "O comando de auditoria é obrigatório"
        );

        Objects.requireNonNull(
            command.evento(),
            "O evento de auditoria é obrigatório"
        );

        Objects.requireNonNull(
            command.resultado(),
            "O resultado da auditoria é obrigatório"
        );
    }

    private String normalizarCampo(
        String value,
        int maximumLength,
        String fieldName
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalizedValue = value.trim();

        if (
            normalizedValue.length()
                > maximumLength
        ) {
            throw new IllegalArgumentException(
                "O campo "
                    + fieldName
                    + " ultrapassa "
                    + maximumLength
                    + " caracteres"
            );
        }

        return normalizedValue;
    }
}