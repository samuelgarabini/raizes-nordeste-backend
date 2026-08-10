package com.raizes.nordeste.pedidos.service;

import com.raizes.nordeste.pedidos.domain.Pagamento;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPagamento;
import com.raizes.nordeste.pedidos.infrastructure.exception.BusinessConflictException;
import com.raizes.nordeste.pedidos.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private static final String MOTIVO_RECUSA_MOCK =
        "Pagamento recusado pelo gateway mock";

    private final PagamentoRepository pagamentoRepository;

    @Transactional
    public Pagamento processar(
        Pedido pedido,
        StatusPagamento resultadoPagamento
    ) {
        validarEntrada(
            pedido,
            resultadoPagamento
        );

        if (pagamentoRepository.existsByPedidoId(pedido.getId())) {
            throw new BusinessConflictException(
                "PAGAMENTO_JA_PROCESSADO",
                "Já existe um pagamento processado para o pedido "
                    + pedido.getId()
            );
        }

        String motivoRecusa =
            resultadoPagamento == StatusPagamento.RECUSADO
                ? MOTIVO_RECUSA_MOCK
                : null;

        Pagamento pagamento = Pagamento.builder()
            .pedidoId(pedido.getId())
            .valor(pedido.getValorTotal())
            .status(resultadoPagamento)
            .motivoRecusa(motivoRecusa)
            .build();

        /*
         * O flush antecipa a validação das restrições únicas,
         * impedindo que pagamentos duplicados permaneçam
         * silenciosamente na transação.
         */
        return pagamentoRepository.saveAndFlush(pagamento);
    }

    private void validarEntrada(
        Pedido pedido,
        StatusPagamento resultadoPagamento
    ) {
        if (pedido == null || pedido.getId() == null) {
            throw new IllegalArgumentException(
                "O pedido do pagamento não pode ser nulo"
            );
        }

        if (pedido.getValorTotal() == null) {
            throw new IllegalArgumentException(
                "O valor do pedido não pode ser nulo"
            );
        }

        if (resultadoPagamento == null) {
            throw new BusinessConflictException(
                "RESULTADO_PAGAMENTO_INVALIDO",
                "O resultado do pagamento não foi informado"
            );
        }
    }
}