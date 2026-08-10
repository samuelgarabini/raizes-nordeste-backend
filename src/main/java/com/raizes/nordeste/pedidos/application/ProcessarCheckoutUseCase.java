package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import com.raizes.nordeste.pedidos.infrastructure.exception.BusinessConflictException;
import com.raizes.nordeste.pedidos.infrastructure.exception.ResourceNotFoundException;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import com.raizes.nordeste.pedidos.service.CampanhaService;
import com.raizes.nordeste.pedidos.service.FidelidadeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProcessarCheckoutUseCase {

    private final PedidoRepository pedidoRepository;
    private final FidelidadeService fidelidadeService;
    private final CampanhaService campanhaService;

    public ProcessarCheckoutUseCase(
        PedidoRepository pedidoRepository,
        FidelidadeService fidelidadeService,
        CampanhaService campanhaService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.fidelidadeService = fidelidadeService;
        this.campanhaService = campanhaService;
    }

    @Transactional
    public Pedido executar(
        UUID pedidoId,
        String codigoPromocional
    ) {
        Pedido pedido = pedidoRepository
            .findByIdForUpdate(pedidoId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "PEDIDO_NAO_ENCONTRADO",
                "Pedido não encontrado: " + pedidoId
            ));

        validarStatusParaCheckout(pedido);

        if (
            codigoPromocional != null
                && !codigoPromocional.isBlank()
        ) {
            BigDecimal desconto = campanhaService.calcularDesconto(
                codigoPromocional,
                pedido.getValorTotal()
            );

            pedido.aplicarDesconto(desconto);
        }

        boolean pagamentoAprovado = true;

        if (pagamentoAprovado) {
            pedido.setStatus(StatusPedido.PAGO);

            fidelidadeService.creditarPontos(
                pedido.getClienteId(),
                pedido.getId(),
                pedido.getValorTotal()
            );
        } else {
            pedido.setStatus(StatusPedido.PAGAMENTO_RECUSADO);
        }

        return pedidoRepository.save(pedido);
    }

    private void validarStatusParaCheckout(Pedido pedido) {
        if (
            pedido.getStatus()
                != StatusPedido.AGUARDANDO_PAGAMENTO
        ) {
            throw new BusinessConflictException(
                "CHECKOUT_NAO_PERMITIDO",
                "O pedido não pode passar pelo checkout no status "
                    + pedido.getStatus()
            );
        }
    }
}