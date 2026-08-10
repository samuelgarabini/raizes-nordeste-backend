package com.raizes.nordeste.pedidos.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    private Integer ordemExibicao;
}