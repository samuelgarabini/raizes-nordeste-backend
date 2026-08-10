package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.application.dto.AtualizarStatusPedidoCommand;
import com.raizes.nordeste.pedidos.application.dto.StatusPedidoResponseDTO;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import com.raizes.nordeste.pedidos.infrastructure.audit.AuditoriaOperacaoService;
import com.raizes.nordeste.pedidos.infrastructure.audit.TipoEventoAuditoria;
import com.raizes.nordeste.pedidos.infrastructure.exception.BusinessConflictException;
import com.raizes.nordeste.pedidos.infrastructure.exception.InvalidRequestException;
import com.raizes.nordeste.pedidos.infrastructure.exception.ResourceNotFoundException;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AtualizarStatusPedidoUseCase {

    private static final String AUDIT_RESOURCE =
        "PEDIDO";

    private static final Map<
        StatusPedido,
        StatusPedido
    > PROXIMO_STATUS = Map.of(
        StatusPedido.PAGO,
        StatusPedido.EM_PREPARACAO,
        StatusPedido.EM_PREPARACAO,
        StatusPedido.PRONTO,
        StatusPedido.PRONTO,
        StatusPedido.ENTREGUE
    );

    private final PedidoRepository pedidoRepository;

    private final AuditoriaOperacaoService
        auditoriaOperacaoService;

    public AtualizarStatusPedidoUseCase(
        PedidoRepository pedidoRepository,
        AuditoriaOperacaoService
            auditoriaOperacaoService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.auditoriaOperacaoService =
            auditoriaOperacaoService;
    }

    @Transactional
    public StatusPedidoResponseDTO executar(
        AtualizarStatusPedidoCommand command
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

            StatusPedido statusAnterior =
                pedido.getStatus();

            validarTransicao(
                statusAnterior,
                command.novoStatus()
            );

            pedido.setStatus(
                command.novoStatus()
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
                        .ALTERACAO_STATUS_PEDIDO,
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

    private void validarCommand(
        AtualizarStatusPedidoCommand command
    ) {
        if (command == null) {
            throw new InvalidRequestException(
                "DADOS_STATUS_INVALIDOS",
                "Os dados da alteração de status "
                    + "não podem ser nulos"
            );
        }

        if (command.pedidoId() == null) {
            throw new InvalidRequestException(
                "PEDIDO_ID_INVALIDO",
                "O identificador do pedido "
                    + "não foi informado"
            );
        }

        if (command.novoStatus() == null) {
            throw new InvalidRequestException(
                "NOVO_STATUS_INVALIDO",
                "O novo status não foi informado"
            );
        }
    }

    private void validarTransicao(
        StatusPedido statusAtual,
        StatusPedido novoStatus
    ) {
        StatusPedido statusEsperado =
            PROXIMO_STATUS.get(statusAtual);

        if (statusEsperado != novoStatus) {
            throw new BusinessConflictException(
                "TRANSICAO_STATUS_INVALIDA",
                "Não é permitido alterar o pedido "
                    + "do status "
                    + statusAtual
                    + " para "
                    + novoStatus
            );
        }
    }

    private UUID obterPedidoId(
        AtualizarStatusPedidoCommand command
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
                        .ALTERACAO_STATUS_PEDIDO,
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