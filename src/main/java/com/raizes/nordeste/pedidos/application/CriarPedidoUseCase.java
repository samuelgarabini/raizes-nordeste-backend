package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.PedidoRepository;
import com.raizes.nordeste.pedidos.infrastructure.messaging.PedidoCriadoEvent;
import com.raizes.nordeste.pedidos.infrastructure.messaging.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CriarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final RabbitTemplate rabbitTemplate;

    public CriarPedidoUseCase(PedidoRepository pedidoRepository, RabbitTemplate rabbitTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Pedido executar(UUID clienteId, UUID unidadeId, CanalPedido canal, BigDecimal valorTotal) {
        // 1. Cria e salva o pedido no PostgreSQL
        Pedido pedido = new Pedido(UUID.randomUUID(), clienteId, unidadeId, canal, valorTotal, "CRIADO");
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // 2. Dispara o evento de integração para a fila do RabbitMQ
        PedidoCriadoEvent event = new PedidoCriadoEvent(
            pedidoSalvo.getId().toString(),
            clienteId.toString(),
            valorTotal
        );

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_PEDIDOS,
            RabbitMQConfig.ROUTING_KEY_PEDIDO_CRIADO,
            event
        );

        return pedidoSalvo;
    }
}