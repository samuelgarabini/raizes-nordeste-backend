package com.raizes.nordeste.infraestrutura.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxMessage> pendingMessages = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxMessage message : pendingMessages) {
            String topic = resolveTopic(message.getType());
            
            kafkaTemplate.send(topic, message.getAggregateId(), message.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        message.setProcessed(true);
                        outboxRepository.save(message);
                        log.info("Evento publicado com sucesso no Kafka. ID: {}", message.getId());
                    } else {
                        log.error("Falha ao publicar evento no Kafka. ID: {}", message.getId(), ex);
                    }
                });
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "PEDIDO_CRIADO" -> "pedidos.criados.v1";
            case "PAGAMENTO_PROCESSADO" -> "pagamentos.processados.v1";
            default -> "eventos.gerais.v1";
        };
    }
}