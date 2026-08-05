package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.domain.*;
import com.raizes.nordeste.pedidos.infrastructure.clients.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CriarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final EstoqueClient estoqueClient;
    private final PagamentoMockClient pagamentoMockClient;
    private final NotificacaoProducer notificacaoProducer;

    @Transactional
    public Pedido executar(CriarPedidoCommand command) {
        command.getItens().forEach(item -> {
            boolean disponivel = estoqueClient.verificarEstoque(
                command.getUnidadeId(), item.getProdutoId(), item.getQuantidade()
            );
            if (!disponivel) {
                throw new EstoqueInsuficienteException("Estoque insuficiente para o produto solicitado.");
            }
        });

        Pedido pedido = new Pedido();
        pedido.setUnidadeId(command.getUnidadeId());
        pedido.setClienteId(command.getClienteId());
        pedido.setCanalPedido(command.getCanalPedido());
        pedido.setFormaPagamento(command.getFormaPagamento());
        pedido.setObservacao(command.getObservacao());

        BigDecimal total = command.getItens().stream()
            .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setTotal(total);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        PagamentoMockResponse resp = pagamentoMockClient.solicitarPagamento(
            pedidoSalvo.getId(), command.getFormaPagamento(), total
        );

        if ("APROVADO".equals(resp.getStatusPagamento())) {
            pedidoSalvo.setStatus(StatusPedido.PAGO);
            pedidoRepository.save(pedidoSalvo);
            estoqueClient.decrementarEstoque(command.getUnidadeId(), command.getItens());
            notificacaoProducer.enviarEventoKDS(pedidoSalvo);
            log.info("Pedido {} criado via canal {}.", pedidoSalvo.getId(), command.getCanalPedido());
        } else {
            pedidoSalvo.setStatus(StatusPedido.PAGAMENTO_RECUSADO);
            pedidoRepository.save(pedidoSalvo);
            throw new PagamentoRecusadoException("Pagamento recusado pelo gateway financeiro.");
        }

        return pedidoSalvo;
    }
}