package com.raizes.nordeste.pedidos.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Pedido {

    private UUID id;
    private UUID clienteId;
    private UUID unidadeId;
    private CanalPedido canalPedido;
    private BigDecimal valorTotal;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}