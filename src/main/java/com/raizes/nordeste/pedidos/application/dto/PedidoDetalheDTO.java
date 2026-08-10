package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import com.raizes.nordeste.pedidos.domain.Pagamento;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import com.raizes.nordeste.pedidos.domain.entity.ItemPedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoDetalheDTO(
    UUID id,
    UUID clienteId,
    UUID unidadeId,
    CanalPedido canalPedido,
    BigDecimal valorTotal,
    StatusPedido status,
    OffsetDateTime dataHora,
    List<ItemPedidoResponseDTO> itens,
    PagamentoResponseDTO pagamento
) {

    public PedidoDetalheDTO {
        itens = itens == null
            ? List.of()
            : List.copyOf(itens);
    }

    public static PedidoDetalheDTO de(
        Pedido pedido,
        List<ItemPedido> itens,
        Pagamento pagamento
    ) {
        List<ItemPedidoResponseDTO> itensResponse =
            itens.stream()
                .map(ItemPedidoResponseDTO::de)
                .toList();

        PagamentoResponseDTO pagamentoResponse =
            pagamento == null
                ? null
                : PagamentoResponseDTO.de(pagamento);

        return new PedidoDetalheDTO(
            pedido.getId(),
            pedido.getClienteId(),
            pedido.getUnidadeId(),
            pedido.getCanalPedido(),
            pedido.getValorTotal(),
            pedido.getStatus(),
            pedido.getDataHora(),
            itensResponse,
            pagamentoResponse
        );
    }
}