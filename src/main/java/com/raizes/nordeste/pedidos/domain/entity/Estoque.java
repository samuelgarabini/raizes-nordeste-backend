package com.raizes.nordeste.pedidos.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "tb_estoques",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_estoques_unidade_produto",
            columnNames = {"unidade_id", "produto_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void prePersist() {
        if (atualizadoEm == null) {
            atualizadoEm = LocalDateTime.now();
        }
    }
}