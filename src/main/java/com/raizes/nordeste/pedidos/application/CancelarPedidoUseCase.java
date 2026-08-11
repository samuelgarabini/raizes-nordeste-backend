package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.application.dto.StatusPedidoResponseDTO;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import com.raizes.nordeste.pedidos.infrastructure.audit.AuditoriaOperacaoService;
import com.raizes.nordeste.pedidos.infrastructure.audit.TipoEventoAuditoria;
import com.raizes.nordeste.pedidos.infrastructure.exception.BusinessConflictException;
import com.raizes.nordeste.pedidos.infrastructure.exception.InvalidRequestException;
import com.raizes.nordeste.pedidos.infrastructure.exception.ResourceNotFoundException;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import com.raizes.nordeste.pedidos.service.EstoqueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CancelarPedidoUseCase {

    private static final String AUDIT_RESOURCE =
        "PEDIDO";

    private final PedidoRepository pedidoRepository;

    private final EstoqueService estoqueService;

    private final AuditoriaOperacaoService
        auditoriaOperacaoService;

    public CancelarPedidoUseCase(
        PedidoRepository pedidoRepository,
        EstoqueService estoqueService,
        AuditoriaOperacaoService
            auditoriaOperacaoService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.estoqueService = estoqueService;
        this.auditoriaOperacaoService =
            auditoriaOperacaoService;
    }

    @Transactional
    public StatusPedidoResponseDTO executar(
        UUID pedidoId
    ) {
        try {
            validarPedidoId(pedidoId);

            Pedido pedido = pedidoRepository
                .findByIdForUpdate(pedidoId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "PEDIDO_NAO_ENCONTRADO",
                        "Pedido não encontrado: "
                            + pedidoId
                    )
                );

            StatusPedido statusAnterior =
                pedido.getStatus();

            validarCancelamento(statusAnterior);

            estoqueService.devolverEstoqueDoPedido(
                pedido
            );

            pedido.setStatus(
                StatusPedido.CANCELADO
            );

            pedidoRepository.saveAndFlush(pedido);

            StatusPedidoResponseDTO response =
                new StatusPedidoResponseDTO(
                    pedido.getId(),
                    statusAnterior,
                    pedido.getStatus()
                );

            auditoriaOperacaoService
                .registrarSucesso(
                    TipoEventoAuditoria
                        .CANCELAMENTO_PEDIDO,
                    AUDIT_RESOURCE,
                    pedido.getId()
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

    private void validarPedidoId(
        UUID pedidoId
    ) {
        if (pedidoId == null) {
            throw new InvalidRequestException(
                "PEDIDO_ID_INVALIDO",
                "O identificador do pedido "
                    + "não foi informado"
            );
        }
    }

    private void validarCancelamento(
        StatusPedido statusAtual
    ) {
        if (
            statusAtual
                != StatusPedido
                    .AGUARDANDO_PAGAMENTO
        ) {
            throw new BusinessConflictException(
                "CANCELAMENTO_NAO_PERMITIDO",
                "O pedido não pode ser cancelado "
                    + "no status "
                    + statusAtual
            );
        }
    }

    private void registrarFalhaSemMascararErro(
        UUID pedidoId,
        RuntimeException originalException
    ) {
        try {
            auditoriaOperacaoService
                .registrarFalha(
                    TipoEventoAuditoria
                        .CANCELAMENTO_PEDIDO,
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
