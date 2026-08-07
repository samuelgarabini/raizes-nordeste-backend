package com.raizes.nordeste.pedidos.service;

import com.raizes.nordeste.pedidos.domain.CarteiraFidelidade;
import com.raizes.nordeste.pedidos.repository.CarteiraFidelidadeRepository;
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

    private final CarteiraFidelidadeRepository carteiraRepository;

    @Transactional
    public void creditarPontos(UUID clienteId, UUID pedidoId, BigDecimal valorGasto) {
        CarteiraFidelidade carteira = carteiraRepository.findByClienteId(clienteId)
                .orElseGet(() -> {
                    // Se o cliente ainda não tiver carteira, cria uma nova automaticamente
                    CarteiraFidelidade novaCarteira = new CarteiraFidelidade();
                    novaCarteira.setClienteId(clienteId);
                    novaCarteira.setPontosAcumulados(0);
                    novaCarteira.setUltimaAtualizacao(LocalDateTime.now());
                    return carteiraRepository.save(novaCarteira);
                });

        // Regra: 1 ponto a cada R$ 10,00 gastos
        int pontosGanhos = valorGasto.divide(new BigDecimal("10"), 0, RoundingMode.DOWN).intValue();

        carteira.setPontosAcumulados(carteira.getPontosAcumulados() + pontosGanhos);
        carteira.setUltimaAtualizacao(LocalDateTime.now());

        carteiraRepository.save(carteira);
    }
}