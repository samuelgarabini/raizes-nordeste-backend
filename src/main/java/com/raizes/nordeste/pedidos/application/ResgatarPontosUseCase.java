package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.application.dto.ResgatarPontosCommand;
import com.raizes.nordeste.pedidos.application.dto.ResgatePontosResponseDTO;
import com.raizes.nordeste.pedidos.infrastructure.audit.AuditoriaOperacaoService;
import com.raizes.nordeste.pedidos.infrastructure.audit.TipoEventoAuditoria;
import com.raizes.nordeste.pedidos.infrastructure.exception.InvalidRequestException;
import com.raizes.nordeste.pedidos.infrastructure.exception.ResourceNotFoundException;
import com.raizes.nordeste.pedidos.infrastructure.security.AutorizacaoClienteService;
import com.raizes.nordeste.pedidos.repository.ClienteRepository;
import com.raizes.nordeste.pedidos.service.FidelidadeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ResgatarPontosUseCase {

    private static final String AUDIT_RESOURCE =
        "CLIENTE";

    private final ClienteRepository clienteRepository;

    private final FidelidadeService fidelidadeService;

    private final AutorizacaoClienteService
        autorizacaoClienteService;

    private final AuditoriaOperacaoService
        auditoriaOperacaoService;

    public ResgatarPontosUseCase(
        ClienteRepository clienteRepository,
        FidelidadeService fidelidadeService,
        AutorizacaoClienteService
            autorizacaoClienteService,
        AuditoriaOperacaoService
            auditoriaOperacaoService
    ) {
        this.clienteRepository = clienteRepository;
        this.fidelidadeService = fidelidadeService;
        this.autorizacaoClienteService =
            autorizacaoClienteService;
        this.auditoriaOperacaoService =
            auditoriaOperacaoService;
    }

    @Transactional
    public ResgatePontosResponseDTO executar(
        ResgatarPontosCommand command
    ) {
        UUID clienteId = obterClienteId(command);

        try {
            validarCommand(command);

            autorizacaoClienteService
                .validarAcessoAoCliente(clienteId);

            validarClienteExistente(clienteId);

            FidelidadeService.ResultadoResgate resultado =
                fidelidadeService.resgatarPontos(
                    clienteId,
                    command.pontos()
                );

            ResgatePontosResponseDTO response =
                new ResgatePontosResponseDTO(
                    resultado.operacaoId(),
                    resultado.clienteId(),
                    resultado.pontosResgatados(),
                    resultado.saldoAnterior(),
                    resultado.saldoAtual(),
                    resultado.resgatadoEm()
                );

            auditoriaOperacaoService.registrarSucesso(
                TipoEventoAuditoria.RESGATE_FIDELIDADE,
                AUDIT_RESOURCE,
                clienteId
            );

            return response;
        } catch (RuntimeException exception) {
            registrarFalhaSemMascararErro(
                clienteId,
                exception
            );

            throw exception;
        }
    }

    private void validarCommand(
        ResgatarPontosCommand command
    ) {
        if (
            command == null
                || command.clienteId() == null
        ) {
            throw new InvalidRequestException(
                "CLIENTE_ID_INVALIDO",
                "O identificador do cliente "
                    + "não foi informado"
            );
        }

        if (command.pontos() <= 0) {
            throw new InvalidRequestException(
                "PONTOS_RESGATE_INVALIDOS",
                "A quantidade de pontos deve ser "
                    + "maior que zero"
            );
        }
    }

    private void validarClienteExistente(
        UUID clienteId
    ) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException(
                "CLIENTE_NAO_ENCONTRADO",
                "Cliente não encontrado: "
                    + clienteId
            );
        }
    }

    private UUID obterClienteId(
        ResgatarPontosCommand command
    ) {
        return command == null
            ? null
            : command.clienteId();
    }

    private void registrarFalhaSemMascararErro(
        UUID clienteId,
        RuntimeException originalException
    ) {
        try {
            auditoriaOperacaoService.registrarFalha(
                TipoEventoAuditoria.RESGATE_FIDELIDADE,
                AUDIT_RESOURCE,
                clienteId,
                originalException
            );
        } catch (RuntimeException auditException) {
            originalException.addSuppressed(
                auditException
            );
        }
    }
}
