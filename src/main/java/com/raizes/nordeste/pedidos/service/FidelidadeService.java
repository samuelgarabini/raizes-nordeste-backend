package com.raizes.nordeste.pedidos.service;

import com.raizes.nordeste.pedidos.domain.CarteiraFidelidade;
import com.raizes.nordeste.pedidos.domain.ConsentimentoFidelidade;
import com.raizes.nordeste.pedidos.domain.HistoricoPontos;
import com.raizes.nordeste.pedidos.domain.TipoOperacaoPontos;
import com.raizes.nordeste.pedidos.infrastructure.exception.BusinessConflictException;
import com.raizes.nordeste.pedidos.repository.CarteiraFidelidadeRepository;
import com.raizes.nordeste.pedidos.repository.ConsentimentoFidelidadeRepository;
import com.raizes.nordeste.pedidos.repository.HistoricoPontosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FidelidadeService {

    private static final BigDecimal VALOR_POR_PONTO =
        new BigDecimal("10");

    private final CarteiraFidelidadeRepository
        carteiraRepository;

    private final HistoricoPontosRepository
        historicoRepository;

    private final ConsentimentoFidelidadeRepository
        consentimentoRepository;

    @Transactional
    public void creditarPontos(
        UUID clienteId,
        UUID pedidoId,
        BigDecimal valorGasto
    ) {
        validarEntrada(clienteId, pedidoId, valorGasto);

        ConsentimentoFidelidade consentimento =
            consentimentoRepository
                .findByClienteIdForUpdate(clienteId)
                .orElse(null);

        if (
            consentimento == null
                || !consentimento.isConcedido()
        ) {
            return;
        }

        carteiraRepository.criarSeAusente(clienteId);

        CarteiraFidelidade carteira = carteiraRepository
            .findByClienteIdForUpdate(clienteId)
            .orElseThrow(() -> new IllegalStateException(
                "Não foi possível criar ou localizar "
                    + "a carteira de fidelidade"
            ));

        boolean creditoJaRegistrado = historicoRepository
            .existsByPedidoIdAndTipoOperacao(
                pedidoId,
                TipoOperacaoPontos.CREDITO
            );

        if (creditoJaRegistrado) {
            return;
        }

        int pontosGanhos = calcularPontos(valorGasto);

        carteira.setPontosAcumulados(
            carteira.getPontosAcumulados() + pontosGanhos
        );

        carteira.setUltimaAtualizacao(
            LocalDateTime.now()
        );

        carteiraRepository.save(carteira);

        HistoricoPontos historico =
            HistoricoPontos.builder()
                .operacaoId(UUID.randomUUID())
                .carteira(carteira)
                .pedidoId(pedidoId)
                .pontosAlterados(pontosGanhos)
                .tipoOperacao(
                    TipoOperacaoPontos.CREDITO
                )
                .build();

        historicoRepository.save(historico);
    }

    @Transactional
    public ResultadoResgate resgatarPontos(
        UUID clienteId,
        int pontos
    ) {
        validarResgate(clienteId, pontos);

        ConsentimentoFidelidade consentimento =
            consentimentoRepository
                .findByClienteIdForUpdate(clienteId)
                .orElseThrow(() ->
                    consentimentoNecessario()
                );

        if (!consentimento.isConcedido()) {
            throw consentimentoNecessario();
        }

        CarteiraFidelidade carteira =
            carteiraRepository
                .findByClienteIdForUpdate(clienteId)
                .orElseThrow(() ->
                    saldoInsuficiente(0, pontos)
                );

        int saldoAnterior =
            carteira.getPontosAcumulados();

        if (saldoAnterior < pontos) {
            throw saldoInsuficiente(
                saldoAnterior,
                pontos
            );
        }

        int saldoAtual = saldoAnterior - pontos;
        LocalDateTime agora = LocalDateTime.now();

        carteira.setPontosAcumulados(saldoAtual);
        carteira.setUltimaAtualizacao(agora);
        carteiraRepository.save(carteira);

        HistoricoPontos historico =
            HistoricoPontos.builder()
                .operacaoId(UUID.randomUUID())
                .carteira(carteira)
                .pedidoId(null)
                .pontosAlterados(pontos)
                .tipoOperacao(
                    TipoOperacaoPontos.DEBITO
                )
                .dataOperacao(agora)
                .build();

        historicoRepository.save(historico);

        return new ResultadoResgate(
            historico.getOperacaoId(),
            clienteId,
            pontos,
            saldoAnterior,
            saldoAtual,
            agora
        );
    }

    private int calcularPontos(BigDecimal valorGasto) {
        return valorGasto
            .divide(
                VALOR_POR_PONTO,
                0,
                RoundingMode.DOWN
            )
            .intValueExact();
    }

    private void validarEntrada(
        UUID clienteId,
        UUID pedidoId,
        BigDecimal valorGasto
    ) {
        if (clienteId == null) {
            throw new IllegalArgumentException(
                "clienteId não pode ser nulo"
            );
        }

        if (pedidoId == null) {
            throw new IllegalArgumentException(
                "pedidoId não pode ser nulo"
            );
        }

        if (
            valorGasto == null
                || valorGasto.compareTo(BigDecimal.ZERO) < 0
        ) {
            throw new IllegalArgumentException(
                "valorGasto deve ser maior ou igual a zero"
            );
        }
    }

    private void validarResgate(
        UUID clienteId,
        int pontos
    ) {
        if (clienteId == null) {
            throw new IllegalArgumentException(
                "clienteId não pode ser nulo"
            );
        }

        if (pontos <= 0) {
            throw new IllegalArgumentException(
                "pontos deve ser maior que zero"
            );
        }
    }

    private BusinessConflictException
        consentimentoNecessario() {

        return new BusinessConflictException(
            "CONSENTIMENTO_FIDELIDADE_NECESSARIO",
            "O cliente deve conceder consentimento "
                + "antes de resgatar pontos"
        );
    }

    private BusinessConflictException saldoInsuficiente(
        int saldo,
        int pontosSolicitados
    ) {
        return new BusinessConflictException(
            "SALDO_PONTOS_INSUFICIENTE",
            "Saldo de "
                + saldo
                + " pontos insuficiente para resgatar "
                + pontosSolicitados
        );
    }

    public record ResultadoResgate(
        UUID operacaoId,
        UUID clienteId,
        int pontosResgatados,
        int saldoAnterior,
        int saldoAtual,
        LocalDateTime resgatadoEm
    ) {
    }
}
