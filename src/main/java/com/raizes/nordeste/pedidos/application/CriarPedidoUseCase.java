package com.raizes.nordeste.pedidos.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raizes.nordeste.pedidos.infrastructure.outbox.OutboxMessage;
import com.raizes.nordeste.pedidos.infrastructure.outbox.OutboxRepository;
import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Pedido executar(UUID clienteId, UUID unidadeId, CanalPedido canal, BigDecimal valorTotal) {
        UUID pedidoId = UUID.randomUUID();

        Pedido pedido = new Pedido(
            pedidoId,
            clienteId,
            unidadeId,
            canal,
            valorTotal,
            "AGUARDANDO_PAGAMENTO"
        );

        // 1. Salva o Pedido na tabela 'pedidos'
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // 2. Salva o Evento na tabela 'tb_outbox'
        executar(pedidoSalvo, pedidoId.toString());

        return pedidoSalvo;
    }

    @Transactional
    public void executar(Object commandPayload, String aggregateId) {
        try {
            String payloadJson = objectMapper.writeValueAsString(commandPayload);

            OutboxMessage outbox = OutboxMessage.builder()
                    .aggregateType("PEDIDO")
                    .aggregateId(aggregateId)
                    .type("PEDIDO_CRIADO")
                    .payload(payloadJson)
                    .build();

            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar evento do pedido", e);
        }
    }
}