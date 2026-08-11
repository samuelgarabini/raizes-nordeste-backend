package com.raizes.nordeste.pedidos.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "campanhas")
@Schema(
    name = "Campanha",
    description =
        "Campanha promocional cadastrada"
)
public class Campanha {

    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    @Schema(
        description =
            "Identificador numérico da campanha",
        example = "1"
    )
    private Long id;

    @Column(nullable = false, length = 150)
    @Schema(
        description =
            "Nome da campanha",
        example =
            "Inauguração Raízes Nordeste"
    )
    private String nome;

    @Column(
        name = "codigo_promocional",
        unique = true,
        length = 50
    )
    @Schema(
        description =
            "Código utilizado no checkout",
        example = "BEMVINDO10"
    )
    private String codigoPromocional;

    @Column(
        name = "desconto_percentual",
        precision = 5,
        scale = 2
    )
    @Schema(
        description =
            "Percentual de desconto",
        example = "10.00"
    )
    private BigDecimal descontoPercentual;

    @Column(
        name = "valor_minimo_pedido",
        precision = 10,
        scale = 2
    )
    @Schema(
        description =
            "Valor mínimo exigido para aplicação",
        example = "50.00"
    )
    private BigDecimal valorMinimoPedido;

    @Column(
        name = "data_inicio",
        nullable = false
    )
    @Schema(
        description =
            "Início da vigência",
        example = "2026-08-10T00:00:00",
        format = "date-time"
    )
    private LocalDateTime dataInicio;

    @Column(
        name = "data_fim",
        nullable = false
    )
    @Schema(
        description =
            "Fim da vigência",
        example = "2026-09-09T00:00:00",
        format = "date-time"
    )
    private LocalDateTime dataFim;

    @Column(nullable = false)
    @Schema(
        description =
            "Indica se a campanha está ativa",
        example = "true"
    )
    private Boolean ativo = true;
}