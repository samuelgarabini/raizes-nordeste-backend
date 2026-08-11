package com.raizes.nordeste.pedidos.infrastructure.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    private UUID id;

    @Column(
        nullable = false,
        length = 100
    )
    private String username;

    @Column(
        name = "senha_hash",
        nullable = false,
        length = 60
    )
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    private Perfil perfil;

    @Column(
        name = "cliente_id",
        unique = true
    )
    private UUID clienteId;

    @Column(nullable = false)
    private boolean ativo;

    @Column(
        name = "criado_em",
        nullable = false,
        updatable = false
    )
    private Instant criadoEm;

    @Column(
        name = "atualizado_em",
        nullable = false
    )
    private Instant atualizadoEm;

    @PrePersist
    protected void prePersist() {
        Instant agora = Instant.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (criadoEm == null) {
            criadoEm = agora;
        }

        atualizadoEm = agora;
    }

    @PreUpdate
    protected void preUpdate() {
        atualizadoEm = Instant.now();
    }
}
