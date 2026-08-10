package com.raizes.nordeste.pedidos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "tb_pagamentos",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_pagamentos_pedido",
            columnNames = "pedido_id"
        ),
        @UniqueConstraint(
            name = "uk_pagamentos_transacao",
            columnNames = "transacao_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {

    private static final String GATEWAY_MOCK =
        "MOCK_GATEWAY";

    @Id
    private UUID id;

    @Column(
        name = "pedido_id",
        nullable = false,
        unique = true
    )
    private UUID pedidoId;

    @Column(
        name = "transacao_id",
        nullable = false,
        unique = true
    )
    private UUID transacaoId;

    @Column(
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    private StatusPagamento status;

    @Column(
        nullable = false,
        length = 50
    )
    private String gateway;

    @Column(
        name = "motivo_recusa",
        length = 255
    )
    private String motivoRecusa;

    @Column(
        name = "processado_em",
        nullable = false
    )
    private LocalDateTime processadoEm;

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (transacaoId == null) {
            transacaoId = UUID.randomUUID();
        }

        if (gateway == null || gateway.isBlank()) {
            gateway = GATEWAY_MOCK;
        }

        if (processadoEm == null) {
            processadoEm = LocalDateTime.now();
        }
    }
}