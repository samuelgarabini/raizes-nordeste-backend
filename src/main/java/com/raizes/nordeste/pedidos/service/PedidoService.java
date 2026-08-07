package com.raizes.nordeste.pedidos.service;

import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final FidelidadeService fidelidadeService;
    private final CampanhaService campanhaService;

    @Transactional
    public Pedido processarCheckout(UUID pedidoId, String codigoPromocional) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // 1. Aplica Campanhas / Códigos Promocionais
        if (codigoPromocional != null && !codigoPromocional.isBlank()) {
            BigDecimal desconto = campanhaService.calcularDesconto(codigoPromocional, pedido.getValorTotal());
            pedido.aplicarDesconto(desconto);
        }

        // 2. Simula o Pagamento
        boolean pagamentoAprovado = simularPagamentoGateway(pedido);
        
        if (pagamentoAprovado) {
            pedido.setStatus("PAGO");
            
            // 3. Credita pontos de fidelidade após o pagamento aprovado
            if (pedido.getClienteId() != null) {
                fidelidadeService.creditarPontos(
                    pedido.getClienteId(), 
                    pedido.getId(), 
                    pedido.getValorTotal()
                );
            }
        } else {
            pedido.setStatus("PAGAMENTO_RECUSADO");
        }

        return pedidoRepository.save(pedido);
    }
    
    private boolean simularPagamentoGateway(Pedido pedido) {
        // Lógica mockada de aprovação de pagamento
        return true; 
    }
}