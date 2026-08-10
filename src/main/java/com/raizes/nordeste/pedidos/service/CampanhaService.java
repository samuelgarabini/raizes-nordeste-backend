package com.raizes.nordeste.pedidos.service;

import com.raizes.nordeste.pedidos.domain.Campanha;
import com.raizes.nordeste.pedidos.infrastructure.exception.BusinessConflictException;
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

    public BigDecimal calcularDesconto(
        String codigoPromocional,
        BigDecimal valorPedido
    ) {
        if (
            codigoPromocional == null
                || codigoPromocional.isBlank()
        ) {
            throw new BusinessConflictException(
                "CUPOM_INVALIDO",
                "O código promocional não foi informado"
            );
        }

        Campanha campanha = campanhaRepository
            .findByCodigoPromocionalIgnoreCaseAndAtivoTrue(
                codigoPromocional.trim()
            )
            .orElseThrow(() -> new BusinessConflictException(
                "CUPOM_INVALIDO",
                "Cupom inválido ou inativo"
            ));

        validarPeriodo(campanha);
        validarValorMinimo(campanha, valorPedido);
        validarPercentual(campanha);

        return valorPedido
            .multiply(campanha.getDescontoPercentual())
            .divide(
                new BigDecimal("100"),
                2,
                RoundingMode.HALF_UP
            );
    }

    private void validarPeriodo(Campanha campanha) {
        LocalDateTime agora = LocalDateTime.now();

        if (
            agora.isBefore(campanha.getDataInicio())
                || agora.isAfter(campanha.getDataFim())
        ) {
            throw new BusinessConflictException(
                "CUPOM_FORA_DA_VALIDADE",
                "O cupom está fora do período de validade"
            );
        }
    }

    private void validarValorMinimo(
        Campanha campanha,
        BigDecimal valorPedido
    ) {
        BigDecimal valorMinimo =
            campanha.getValorMinimoPedido() == null
                ? BigDecimal.ZERO
                : campanha.getValorMinimoPedido();

        if (valorPedido.compareTo(valorMinimo) < 0) {
            throw new BusinessConflictException(
                "VALOR_MINIMO_NAO_ATINGIDO",
                "O valor mínimo para este cupom é R$ "
                    + valorMinimo
            );
        }
    }

    private void validarPercentual(Campanha campanha) {
        BigDecimal percentual =
            campanha.getDescontoPercentual();

        if (
            percentual == null
                || percentual.compareTo(BigDecimal.ZERO) <= 0
                || percentual.compareTo(
                    new BigDecimal("100")
                ) > 0
        ) {
            throw new BusinessConflictException(
                "CUPOM_INVALIDO",
                "O percentual de desconto do cupom é inválido"
            );
        }
    }
}