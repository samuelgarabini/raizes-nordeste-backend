package com.raizes.nordeste.pedidos.service;

import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.entity.ItemPedido;
import com.raizes.nordeste.pedidos.repository.EstoqueRepository;
import com.raizes.nordeste.pedidos.repository.ItemPedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final ItemPedidoRepository itemPedidoRepository;
    private final EstoqueRepository estoqueRepository;

    @Transactional
    public void devolverEstoqueDoPedido(
        Pedido pedido
    ) {
        if (pedido == null || pedido.getId() == null) {
            throw new IllegalArgumentException(
                "O pedido não pode ser nulo"
            );
        }

        List<ItemPedido> itens =
            itemPedidoRepository.findByPedidoId(
                pedido.getId()
            );

        if (itens.isEmpty()) {
            throw new IllegalStateException(
                "Nenhum item foi encontrado para o pedido "
                    + pedido.getId()
            );
        }

        for (ItemPedido item : itens) {
            int registrosAtualizados =
                estoqueRepository.devolverEstoque(
                    pedido.getUnidadeId(),
                    item.getProdutoId(),
                    item.getQuantidade()
                );

            if (registrosAtualizados != 1) {
                throw new IllegalStateException(
                    "Não foi possível devolver ao estoque "
                        + "o produto "
                        + item.getProdutoId()
                        + " do pedido "
                        + pedido.getId()
                );
            }
        }
    }
}