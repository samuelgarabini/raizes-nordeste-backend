package com.raizes.nordeste.pedidos.domain;

public enum StatusPedido {
    AGUARDANDO_PAGAMENTO,
    PAGO,
    PAGAMENTO_RECUSADO,
    EM_PREPARACAO,
    PRONTO,
    ENTREGUE,
    CANCELADO
}