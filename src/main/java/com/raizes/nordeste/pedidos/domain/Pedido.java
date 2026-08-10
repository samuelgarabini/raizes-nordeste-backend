package com.raizes.nordeste.pedidos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_pedidos")
public class Pedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_pedido", nullable = false)
    private CanalPedido canalPedido;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    public Pedido() {
    }

    public Pedido(
        UUID id,
        UUID clienteId,
        UUID unidadeId,
        CanalPedido canalPedido,
        BigDecimal valorTotal,
        StatusPedido status
    ) {
        this.id = id;
        this.clienteId = clienteId;
        this.unidadeId = unidadeId;
        this.canalPedido = canalPedido;
        this.valorTotal = valorTotal;
        this.status = status;
    }

    public void aplicarDesconto(BigDecimal valorDesconto) {
        if (
            valorDesconto != null
                && valorDesconto.compareTo(BigDecimal.ZERO) > 0
        ) {
            this.valorTotal = this.valorTotal.subtract(valorDesconto);

            if (this.valorTotal.compareTo(BigDecimal.ZERO) < 0) {
                this.valorTotal = BigDecimal.ZERO;
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public void setClienteId(UUID clienteId) {
        this.clienteId = clienteId;
    }

    public UUID getUnidadeId() {
        return unidadeId;
    }

    public void setUnidadeId(UUID unidadeId) {
        this.unidadeId = unidadeId;
    }

    public CanalPedido getCanalPedido() {
        return canalPedido;
    }

    public void setCanalPedido(CanalPedido canalPedido) {
        this.canalPedido = canalPedido;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }
}