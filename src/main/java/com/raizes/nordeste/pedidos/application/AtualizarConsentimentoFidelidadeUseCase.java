package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.application.dto.AtualizarConsentimentoFidelidadeCommand;
import com.raizes.nordeste.pedidos.application.dto.ConsentimentoFidelidadeResponseDTO;
import com.raizes.nordeste.pedidos.domain.ConsentimentoFidelidade;
import com.raizes.nordeste.pedidos.infrastructure.audit.AuditoriaOperacaoService;
import com.raizes.nordeste.pedidos.infrastructure.audit.TipoEventoAuditoria;
import com.raizes.nordeste.pedidos.infrastructure.exception.InvalidRequestException;
import com.raizes.nordeste.pedidos.infrastructure.exception.ResourceNotFoundException;
import com.raizes.nordeste.pedidos.infrastructure.security.AutorizacaoClienteService;
import com.raizes.nordeste.pedidos.repository.ClienteRepository;
import com.raizes.nordeste.pedidos.repository.ConsentimentoFidelidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AtualizarConsentimentoFidelidadeUseCase {

    private static final String AUDIT_RESOURCE =
        "CLIENTE";

    private final ClienteRepository clienteRepository;

    private final ConsentimentoFidelidadeRepository
        consentimentoRepository;

    private final AutorizacaoClienteService
        autorizacaoClienteService;

    private final AuditoriaOperacaoService
        auditoriaOperacaoService;

    public AtualizarConsentimentoFidelidadeUseCase(
        ClienteRepository clienteRepository,
        ConsentimentoFidelidadeRepository
            consentimentoRepository,
        AutorizacaoClienteService
            autorizacaoClienteService,
        AuditoriaOperacaoService
            auditoriaOperacaoService
    ) {
        this.clienteRepository = clienteRepository;
        this.consentimentoRepository =
            consentimentoRepository;
        this.autorizacaoClienteService =
            autorizacaoClienteService;
        this.auditoriaOperacaoService =
            auditoriaOperacaoService;
    }

    @Transactional
    public ConsentimentoFidelidadeResponseDTO executar(
        AtualizarConsentimentoFidelidadeCommand command
    ) {
        UUID clienteId = obterClienteId(command);
        TipoEventoAuditoria evento = obterEvento(command);

        try {
            validarCommand(command);

            autorizacaoClienteService
                .validarAcessoAoCliente(clienteId);

            validarClienteExistente(clienteId);

            ConsentimentoFidelidade consentimento =
                consentimentoRepository
                    .findByClienteIdForUpdate(clienteId)
                    .orElseGet(() ->
                        ConsentimentoFidelidade.builder()
                            .clienteId(clienteId)
                            .build()
                    );

            consentimento.atualizar(
                command.concedido(),
                command.versaoTermo().trim()
            );

            ConsentimentoFidelidade salvo =
                consentimentoRepository.saveAndFlush(
                    consentimento
                );

            auditoriaOperacaoService.registrarSucesso(
                evento,
                AUDIT_RESOURCE,
                clienteId
            );

            return ConsentimentoFidelidadeResponseDTO
                .de(salvo);
        } catch (RuntimeException exception) {
            registrarFalhaSemMascararErro(
                evento,
                clienteId,
                exception
            );

            throw exception;
        }
    }

    private void validarCommand(
        AtualizarConsentimentoFidelidadeCommand command
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

        if (
            command.versaoTermo() == null
                || command.versaoTermo().isBlank()
                || command.versaoTermo().trim().length()
                    > 50
        ) {
            throw new InvalidRequestException(
                "VERSAO_TERMO_INVALIDA",
                "A versão do termo deve possuir "
                    + "entre 1 e 50 caracteres"
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
        AtualizarConsentimentoFidelidadeCommand command
    ) {
        return command == null
            ? null
            : command.clienteId();
    }

    private TipoEventoAuditoria obterEvento(
        AtualizarConsentimentoFidelidadeCommand command
    ) {
        return command != null && command.concedido()
            ? TipoEventoAuditoria
                .CONSENTIMENTO_FIDELIDADE_CONCEDIDO
            : TipoEventoAuditoria
                .CONSENTIMENTO_FIDELIDADE_REVOGADO;
    }

    private void registrarFalhaSemMascararErro(
        TipoEventoAuditoria evento,
        UUID clienteId,
        RuntimeException originalException
    ) {
        try {
            auditoriaOperacaoService.registrarFalha(
                evento,
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
