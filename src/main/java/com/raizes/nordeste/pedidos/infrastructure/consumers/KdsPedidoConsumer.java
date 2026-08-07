package com.raizes.nordeste.pedidos.infrastructure.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KdsPedidoConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "pedidos.criados.v1",
        groupId = "kds-cozinha-group"
    )
    public void consumirPedidoCriado(String payload) {
        try {
            log.info(" [KDS COZINHA] Novo pedido recebido via Kafka: {}", payload);
            
            // Aqui o KDS atualiza o status ou notifica a tela da cozinha em tempo real
            // Exemplo: webSocketTemplate.convertAndSend("/topic/cozinha", payload);

        } catch (Exception e) {
            log.error(" [KDS COZINHA] Erro ao processar evento de pedido", e);
        }
    }
}