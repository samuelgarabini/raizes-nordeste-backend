package com.raizes.nordeste.pedidos.service;

import com.raizes.nordeste.pedidos.domain.CarteiraFidelidade;
import com.raizes.nordeste.pedidos.domain.HistoricoPontos;
import com.raizes.nordeste.pedidos.domain.TipoOperacaoPontos;
import com.raizes.nordeste.pedidos.repository.CarteiraFidelidadeRepository;
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

    @Transactional
    public void creditarPontos(
        UUID clienteId,
        UUID pedidoId,
        BigDecimal valorGasto
    ) {
        validarEntrada(clienteId, pedidoId, valorGasto);

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
                .carteira(carteira)
                .pedidoId(pedidoId)
                .pontosAlterados(pontosGanhos)
                .tipoOperacao(
                    TipoOperacaoPontos.CREDITO
                )
                .build();

        historicoRepository.save(historico);
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
}