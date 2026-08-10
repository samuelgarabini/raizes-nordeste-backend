package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.application.dto.CheckoutResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.ProcessarCheckoutCommand;
import com.raizes.nordeste.pedidos.domain.Pagamento;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPagamento;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import com.raizes.nordeste.pedidos.infrastructure.audit.AuditoriaOperacaoService;
import com.raizes.nordeste.pedidos.infrastructure.audit.TipoEventoAuditoria;
import com.raizes.nordeste.pedidos.infrastructure.exception.BusinessConflictException;
import com.raizes.nordeste.pedidos.infrastructure.exception.ResourceNotFoundException;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import com.raizes.nordeste.pedidos.service.CampanhaService;
import com.raizes.nordeste.pedidos.service.EstoqueService;
import com.raizes.nordeste.pedidos.service.FidelidadeService;
import com.raizes.nordeste.pedidos.service.PagamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProcessarCheckoutUseCase {

    private static final String AUDIT_RESOURCE =
        "PEDIDO";

    private final PedidoRepository pedidoRepository;

    private final FidelidadeService fidelidadeService;

    private final CampanhaService campanhaService;

    private final PagamentoService pagamentoService;

    private final EstoqueService estoqueService;

    private final AuditoriaOperacaoService
        auditoriaOperacaoService;

    public ProcessarCheckoutUseCase(
        PedidoRepository pedidoRepository,
        FidelidadeService fidelidadeService,
        CampanhaService campanhaService,
        PagamentoService pagamentoService,
        EstoqueService estoqueService,
        AuditoriaOperacaoService
            auditoriaOperacaoService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.fidelidadeService = fidelidadeService;
        this.campanhaService = campanhaService;
        this.pagamentoService = pagamentoService;
        this.estoqueService = estoqueService;
        this.auditoriaOperacaoService =
            auditoriaOperacaoService;
    }

    @Transactional
    public CheckoutResponseDTO executar(
        ProcessarCheckoutCommand command
    ) {
        UUID pedidoId = obterPedidoId(command);

        try {
            validarCommand(command);

            Pedido pedido = pedidoRepository
                .findByIdForUpdate(
                    command.pedidoId()
                )
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "PEDIDO_NAO_ENCONTRADO",
                        "Pedido não encontrado: "
                            + command.pedidoId()
                    )
                );

            validarStatusParaCheckout(pedido);

            aplicarCupom(
                pedido,
                command.codigoPromocional()
            );

            Pagamento pagamento =
                pagamentoService.processar(
                    pedido,
                    command.resultadoPagamento()
                );

            if (
                command.resultadoPagamento()
                    == StatusPagamento.APROVADO
            ) {
                processarPagamentoAprovado(pedido);
            } else {
                processarPagamentoRecusado(pedido);
            }

            Pedido pedidoAtualizado =
                pedidoRepository.saveAndFlush(
                    pedido
                );

            CheckoutResponseDTO response =
                CheckoutResponseDTO.de(
                    pedidoAtualizado,
                    pagamento
                );

            auditoriaOperacaoService
                .registrarSucesso(
                    TipoEventoAuditoria
                        .CHECKOUT_PEDIDO,
                    AUDIT_RESOURCE,
                    pedidoAtualizado.getId()
                );

            return response;
        } catch (RuntimeException exception) {
            registrarFalhaSemMascararErro(
                pedidoId,
                exception
            );

            throw exception;
        }
    }

    private void aplicarCupom(
        Pedido pedido,
        String codigoPromocional
    ) {
        if (
            codigoPromocional == null
                || codigoPromocional.isBlank()
        ) {
            return;
        }

        BigDecimal desconto =
            campanhaService.calcularDesconto(
                codigoPromocional,
                pedido.getValorTotal()
            );

        pedido.aplicarDesconto(desconto);
    }

    private void processarPagamentoAprovado(
        Pedido pedido
    ) {
        pedido.setStatus(StatusPedido.PAGO);

        fidelidadeService.creditarPontos(
            pedido.getClienteId(),
            pedido.getId(),
            pedido.getValorTotal()
        );
    }

    private void processarPagamentoRecusado(
        Pedido pedido
    ) {
        pedido.setStatus(
            StatusPedido.PAGAMENTO_RECUSADO
        );

        estoqueService.devolverEstoqueDoPedido(
            pedido
        );
    }

    private void validarCommand(
        ProcessarCheckoutCommand command
    ) {
        if (command == null) {
            throw new IllegalArgumentException(
                "Os dados do checkout "
                    + "não podem ser nulos"
            );
        }

        if (command.pedidoId() == null) {
            throw new IllegalArgumentException(
                "O identificador do pedido "
                    + "não pode ser nulo"
            );
        }

        if (
            command.resultadoPagamento()
                == null
        ) {
            throw new BusinessConflictException(
                "RESULTADO_PAGAMENTO_INVALIDO",
                "O resultado do pagamento "
                    + "não foi informado"
            );
        }
    }

    private void validarStatusParaCheckout(
        Pedido pedido
    ) {
        if (
            pedido.getStatus()
                != StatusPedido
                    .AGUARDANDO_PAGAMENTO
        ) {
            throw new BusinessConflictException(
                "CHECKOUT_NAO_PERMITIDO",
                "O pedido não pode passar "
                    + "pelo checkout no status "
                    + pedido.getStatus()
            );
        }
    }

    private UUID obterPedidoId(
        ProcessarCheckoutCommand command
    ) {
        return command == null
            ? null
            : command.pedidoId();
    }

    private void registrarFalhaSemMascararErro(
        UUID pedidoId,
        RuntimeException originalException
    ) {
        try {
            auditoriaOperacaoService
                .registrarFalha(
                    TipoEventoAuditoria
                        .CHECKOUT_PEDIDO,
                    AUDIT_RESOURCE,
                    pedidoId,
                    originalException
                );
        } catch (RuntimeException auditException) {
            originalException.addSuppressed(
                auditException
            );
        }
    }
}