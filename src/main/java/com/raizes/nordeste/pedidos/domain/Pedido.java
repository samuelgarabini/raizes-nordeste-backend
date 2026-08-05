package com.raizes.nordeste.pedidos.domain;

import jakarta.persistence.*;
import java.io.Serializable; // Import do Serializable
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
public class Pedido implements Serializable { // Implementa Serializable

    private static final long serialVersionUID = 1L; // Identificador da versão de serialização

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

    @Column(nullable = false)
    private String status;

    public Pedido() {
    }

    public Pedido(UUID id, UUID clienteId, UUID unidadeId, CanalPedido canalPedido, BigDecimal valorTotal, String status) {
        this.id = id;
        this.clienteId = clienteId;
        this.unidadeId = unidadeId;
        this.canalPedido = canalPedido;
        this.valorTotal = valorTotal;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }

    public UUID getUnidadeId() { return unidadeId; }
    public void setUnidadeId(UUID unidadeId) { this.unidadeId = unidadeId; }

    public CanalPedido getCanalPedido() { return canalPedido; }
    public void setCanalPedido(CanalPedido canalPedido) { this.canalPedido = canalPedido; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}