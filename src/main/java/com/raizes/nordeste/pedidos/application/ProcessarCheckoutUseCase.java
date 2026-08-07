package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import com.raizes.nordeste.pedidos.service.CampanhaService;
import com.raizes.nordeste.pedidos.service.FidelidadeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProcessarCheckoutUseCase {

    private final PedidoRepository pedidoRepository;
    private final FidelidadeService fidelidadeService;
    private final CampanhaService campanhaService;

    public ProcessarCheckoutUseCase(PedidoRepository pedidoRepository,
                                    FidelidadeService fidelidadeService,
                                    CampanhaService campanhaService) {
        this.pedidoRepository = pedidoRepository;
        this.fidelidadeService = fidelidadeService;
        this.campanhaService = campanhaService;
    }

    @Transactional
    public Pedido executar(UUID pedidoId, String codigoPromocional) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // 1. Aplica Campanhas / Códigos Promocionais
        if (codigoPromocional != null && !codigoPromocional.isBlank()) {
            BigDecimal desconto = campanhaService.calcularDesconto(codigoPromocional, pedido.getValorTotal());
            pedido.aplicarDesconto(desconto);
        }

        // 2. Simula o Pagamento
        boolean pagamentoAprovado = true; // Mockado como aprovado
        
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
}