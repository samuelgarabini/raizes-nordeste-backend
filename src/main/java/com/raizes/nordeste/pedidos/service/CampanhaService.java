package com.raizes.nordeste.pedidos.service;

import com.raizes.nordeste.pedidos.domain.Campanha;
import com.raizes.nordeste.pedidos.repository.CampanhaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CampanhaService {

    private final CampanhaRepository campanhaRepository;

    public BigDecimal calcularDesconto(String codigoPromocional, BigDecimal valorPedido) {
        Campanha campanha = campanhaRepository.findByCodigoPromocionalAndAtivoTrue(codigoPromocional)
                .orElseThrow(() -> new RuntimeException("Cupom inválido ou expirado"));

        LocalDateTime agora = LocalDateTime.now();
        if (agora.isBefore(campanha.getDataInicio()) || agora.isAfter(campanha.getDataFim())) {
            throw new RuntimeException("Este cupom está fora do período de validade");
        }

        if (valorPedido.compareTo(campanha.getValorMinimoPedido()) < 0) {
            throw new RuntimeException("O valor mínimo para este cupom é de R$ " + campanha.getValorMinimoPedido());
        }

        // Calcula o desconto percentual sobre o valor do pedido
        return valorPedido
                .multiply(campanha.getDescontoPercentual())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}