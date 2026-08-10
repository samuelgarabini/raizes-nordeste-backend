package com.raizes.nordeste.pedidos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "historico_pontos",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_historico_pontos_pedido_operacao",
            columnNames = {
                "pedido_id",
                "tipo_operacao"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoPontos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carteira_id", nullable = false)
    private CarteiraFidelidade carteira;

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Column(name = "pontos_alterados", nullable = false)
    private Integer pontosAlterados;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "tipo_operacao",
        nullable = false,
        length = 20
    )
    private TipoOperacaoPontos tipoOperacao;

    @Column(name = "data_operacao", nullable = false)
    private LocalDateTime dataOperacao;

    @PrePersist
    protected void prePersist() {
        if (dataOperacao == null) {
            dataOperacao = LocalDateTime.now();
        }
    }
}