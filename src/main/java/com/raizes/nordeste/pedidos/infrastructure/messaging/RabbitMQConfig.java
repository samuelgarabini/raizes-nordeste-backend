package com.raizes.nordeste.pedidos.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_PEDIDOS = "pedidos.v1.pedido-criado";
    public static final String EXCHANGE_PEDIDOS = "pedidos.v1.events";
    public static final String ROUTING_KEY_PEDIDO_CRIADO = "pedido.criado";

    @Bean
    public Queue pedidosQueue() {
        return QueueBuilder.durable(QUEUE_PEDIDOS).build();
    }

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(EXCHANGE_PEDIDOS);
    }

    @Bean
    public Binding pedidosBinding(Queue pedidosQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(pedidosQueue).to(pedidosExchange).with(ROUTING_KEY_PEDIDO_CRIADO);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}