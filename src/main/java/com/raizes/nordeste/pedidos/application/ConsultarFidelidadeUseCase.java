package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.application.dto.ConsentimentoFidelidadeResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.FidelidadeResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.HistoricoPontosResponseDTO;
import com.raizes.nordeste.pedidos.domain.CarteiraFidelidade;
import com.raizes.nordeste.pedidos.infrastructure.audit.AuditoriaOperacaoService;
import com.raizes.nordeste.pedidos.infrastructure.audit.TipoEventoAuditoria;
import com.raizes.nordeste.pedidos.infrastructure.exception.InvalidRequestException;
import com.raizes.nordeste.pedidos.infrastructure.exception.ResourceNotFoundException;
import com.raizes.nordeste.pedidos.infrastructure.security.AutorizacaoClienteService;
import com.raizes.nordeste.pedidos.repository.CarteiraFidelidadeRepository;
import com.raizes.nordeste.pedidos.repository.ClienteRepository;
import com.raizes.nordeste.pedidos.repository.ConsentimentoFidelidadeRepository;
import com.raizes.nordeste.pedidos.repository.HistoricoPontosRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultarFidelidadeUseCase {

    private static final String AUDIT_RESOURCE =
        "CLIENTE";

    private final ClienteRepository clienteRepository;

    private final CarteiraFidelidadeRepository
        carteiraRepository;

    private final ConsentimentoFidelidadeRepository
        consentimentoRepository;

    private final HistoricoPontosRepository
        historicoRepository;

    private final AutorizacaoClienteService
        autorizacaoClienteService;

    private final AuditoriaOperacaoService
        auditoriaOperacaoService;

    public ConsultarFidelidadeUseCase(
        ClienteRepository clienteRepository,
        CarteiraFidelidadeRepository carteiraRepository,
        ConsentimentoFidelidadeRepository
            consentimentoRepository,
        HistoricoPontosRepository historicoRepository,
        AutorizacaoClienteService
            autorizacaoClienteService,
        AuditoriaOperacaoService
            auditoriaOperacaoService
    ) {
        this.clienteRepository = clienteRepository;
        this.carteiraRepository = carteiraRepository;
        this.consentimentoRepository =
            consentimentoRepository;
        this.historicoRepository = historicoRepository;
        this.autorizacaoClienteService =
            autorizacaoClienteService;
        this.auditoriaOperacaoService =
            auditoriaOperacaoService;
    }

    @Transactional(readOnly = true)
    public FidelidadeResponseDTO executar(
        UUID clienteId
    ) {
        try {
            validarClienteId(clienteId);

            autorizacaoClienteService
                .validarAcessoAoCliente(clienteId);

            validarClienteExistente(clienteId);

            CarteiraFidelidade carteira =
                carteiraRepository
                    .findByClienteId(clienteId)
                    .orElse(null);

            List<HistoricoPontosResponseDTO> historico =
                carteira == null
                    ? List.of()
                    : historicoRepository
                        .findByCarteiraIdOrderByDataOperacaoDesc(
                            carteira.getId()
                        )
                        .stream()
                        .map(
                            HistoricoPontosResponseDTO::de
                        )
                        .toList();

            ConsentimentoFidelidadeResponseDTO
                consentimento = consentimentoRepository
                    .findByClienteId(clienteId)
                    .map(
                        ConsentimentoFidelidadeResponseDTO::de
                    )
                    .orElseGet(() ->
                        ConsentimentoFidelidadeResponseDTO
                            .naoRegistrado(clienteId)
                    );

            FidelidadeResponseDTO response =
                new FidelidadeResponseDTO(
                    clienteId,
                    carteira == null
                        ? 0
                        : carteira.getPontosAcumulados(),
                    carteira == null
                        ? null
                        : carteira.getUltimaAtualizacao(),
                    consentimento,
                    historico
                );

            auditoriaOperacaoService.registrarSucesso(
                TipoEventoAuditoria
                    .CONSULTA_FIDELIDADE,
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

    private void validarClienteId(UUID clienteId) {
        if (clienteId == null) {
            throw new InvalidRequestException(
                "CLIENTE_ID_INVALIDO",
                "O identificador do cliente "
                    + "não foi informado"
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

    private void registrarFalhaSemMascararErro(
        UUID clienteId,
        RuntimeException originalException
    ) {
        try {
            auditoriaOperacaoService.registrarFalha(
                TipoEventoAuditoria
                    .CONSULTA_FIDELIDADE,
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
