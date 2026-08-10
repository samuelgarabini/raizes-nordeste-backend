package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.application.dto.PedidoDetalheDTO;
import com.raizes.nordeste.pedidos.domain.Pagamento;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.entity.ItemPedido;
import com.raizes.nordeste.pedidos.infrastructure.exception.ResourceNotFoundException;
import com.raizes.nordeste.pedidos.repository.ItemPedidoRepository;
import com.raizes.nordeste.pedidos.repository.PagamentoRepository;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class BuscarPedidoPorIdUseCase {

    private final PedidoRepository pedidoRepository;

    private final ItemPedidoRepository
        itemPedidoRepository;

    private final PagamentoRepository
        pagamentoRepository;

    public BuscarPedidoPorIdUseCase(
        PedidoRepository pedidoRepository,
        ItemPedidoRepository itemPedidoRepository,
        PagamentoRepository pagamentoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository =
            itemPedidoRepository;
        this.pagamentoRepository =
            pagamentoRepository;
    }

    @Transactional(readOnly = true)
    public PedidoDetalheDTO executar(
        UUID pedidoId
    ) {
        Pedido pedido = pedidoRepository
            .findById(pedidoId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "PEDIDO_NAO_ENCONTRADO",
                    "Pedido não encontrado: "
                        + pedidoId
                )
            );

        List<ItemPedido> itens =
            itemPedidoRepository
                .findByPedidoId(pedidoId)
                .stream()
                .sorted(
                    Comparator.comparing(
                        ItemPedido::getId
                    )
                )
                .toList();

        Pagamento pagamento =
            pagamentoRepository
                .findByPedidoId(pedidoId)
                .orElse(null);

        return PedidoDetalheDTO.de(
            pedido,
            itens,
            pagamento
        );
    }
}