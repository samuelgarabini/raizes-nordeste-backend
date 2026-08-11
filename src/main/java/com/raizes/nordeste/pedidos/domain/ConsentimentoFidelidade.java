package com.raizes.nordeste.pedidos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "consentimentos_fidelidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentimentoFidelidade {

    public static final String FINALIDADE =
        "PROGRAMA_FIDELIDADE";

    public static final String BASE_LEGAL =
        "CONSENTIMENTO";

    @Id
    private UUID id;

    @Column(
        name = "cliente_id",
        nullable = false,
        unique = true
    )
    private UUID clienteId;

    @Column(nullable = false)
    private boolean concedido;

    @Column(
        name = "versao_termo",
        nullable = false,
        length = 50
    )
    private String versaoTermo;

    @Column(nullable = false, length = 50)
    private String finalidade;

    @Column(
        name = "base_legal",
        nullable = false,
        length = 30
    )
    private String baseLegal;

    @Column(name = "concedido_em")
    private OffsetDateTime concedidoEm;

    @Column(name = "revogado_em")
    private OffsetDateTime revogadoEm;

    @Column(
        name = "atualizado_em",
        nullable = false
    )
    private OffsetDateTime atualizadoEm;

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (finalidade == null) {
            finalidade = FINALIDADE;
        }

        if (baseLegal == null) {
            baseLegal = BASE_LEGAL;
        }

        if (atualizadoEm == null) {
            atualizadoEm = agora();
        }
    }

    public void atualizar(
        boolean novoConsentimento,
        String novaVersaoTermo
    ) {
        OffsetDateTime instante = agora();

        concedido = novoConsentimento;
        versaoTermo = novaVersaoTermo;
        finalidade = FINALIDADE;
        baseLegal = BASE_LEGAL;
        atualizadoEm = instante;

        if (novoConsentimento) {
            concedidoEm = instante;
            revogadoEm = null;
        } else {
            revogadoEm = instante;
        }
    }

    private OffsetDateTime agora() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
