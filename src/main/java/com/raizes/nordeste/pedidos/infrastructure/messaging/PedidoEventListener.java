package com.raizes.nordeste.pedidos.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDOS)
    public void onPedidoCriado(PedidoCriadoEvent event) {
        System.out.println("==================================================");
        System.out.println(">>> [RabbitMQ] EVENTO RECEBIDO COM SUCESSO!");
        System.out.println(">>> ID do Pedido: " + event.pedidoId());
        System.out.println(">>> Cliente: " + event.clienteId());
        System.out.println(">>> Valor Total: R$ " + event.valorTotal());
        System.out.println("==================================================");
    }
}