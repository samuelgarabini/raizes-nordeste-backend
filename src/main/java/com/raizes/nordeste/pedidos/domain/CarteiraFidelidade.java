package com.raizes.nordeste.pedidos.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "carteiras_fidelidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteiraFidelidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false, unique = true)
    private java.util.UUID clienteId;

    @Column(name = "pontos_acumulados", nullable = false)
    private Integer pontosAcumulados = 0;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao = LocalDateTime.now();
}