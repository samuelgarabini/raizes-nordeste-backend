package com.raizes.nordeste.pedidos.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
    name = "tb_pedido_itens",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_pedido_itens_pedido_produto",
            columnNames = {"pedido_id", "produto_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(
        name = "preco_unitario",
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}