package com.raizes.nordeste.pedidos.infrastructure.audit;

import com.raizes.nordeste.pedidos.infrastructure.security.Perfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_auditoria_seguranca")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuditoriaSeguranca {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 50
    )
    private TipoEventoAuditoria evento;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    private ResultadoAuditoria resultado;

    @Column(
        name = "ator_fingerprint",
        length = 64
    )
    private String atorFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Perfil perfil;

    @Column(length = 50)
    private String recurso;

    @Column(
        name = "recurso_id",
        length = 100
    )
    private String recursoId;

    @Column(
        name = "ip_fingerprint",
        length = 64
    )
    private String ipFingerprint;

    @Column(
        name = "codigo_erro",
        length = 100
    )
    private String codigoErro;

    @Column(
        name = "ocorrido_em",
        nullable = false,
        updatable = false
    )
    private Instant ocorridoEm;

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (ocorridoEm == null) {
            ocorridoEm = Instant.now();
        }
    }
}