package com.raizes.nordeste.pedidos.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "campanhas")
public class Campanha {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "codigo_promocional", unique = true, length = 50)
    private String codigoPromocional;

    @Column(name = "desconto_percentual", precision = 5, scale = 2)
    private BigDecimal descontoPercentual;

    @Column(name = "valor_minimo_pedido", precision = 10, scale = 2)
    private BigDecimal valorMinimoPedido;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Column(nullable = false)
    private Boolean ativo = true;
}