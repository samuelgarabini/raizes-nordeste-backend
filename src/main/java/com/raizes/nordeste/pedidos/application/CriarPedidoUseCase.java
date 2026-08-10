package com.raizes.nordeste.pedidos.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raizes.nordeste.pedidos.application.dto.CriarPedidoCommand;
import com.raizes.nordeste.pedidos.application.dto.PedidoCriadoDTO;
import com.raizes.nordeste.pedidos.domain.Pedido;
import com.raizes.nordeste.pedidos.domain.StatusPedido;
import com.raizes.nordeste.pedidos.domain.entity.ItemPedido;
import com.raizes.nordeste.pedidos.domain.entity.Produto;
import com.raizes.nordeste.pedidos.infrastructure.exception.BusinessConflictException;
import com.raizes.nordeste.pedidos.infrastructure.exception.ResourceNotFoundException;
import com.raizes.nordeste.pedidos.infrastructure.outbox.OutboxMessage;
import com.raizes.nordeste.pedidos.infrastructure.outbox.OutboxRepository;
import com.raizes.nordeste.pedidos.infrastructure.repository.ProdutoRepository;
import com.raizes.nordeste.pedidos.repository.ClienteRepository;
import com.raizes.nordeste.pedidos.repository.EstoqueRepository;
import com.raizes.nordeste.pedidos.repository.ItemPedidoRepository;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import com.raizes.nordeste.pedidos.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public PedidoCriadoDTO executar(CriarPedidoCommand command) {
        validarCliente(command.clienteId());
        validarUnidade(command.unidadeId());

        UUID pedidoId = UUID.randomUUID();
        BigDecimal valorTotal = BigDecimal.ZERO;

        Set<Long> produtosAdicionados = new HashSet<>();
        List<ItemPedido> itensParaPersistir = new ArrayList<>();
        List<PedidoCriadoDTO.Item> itensDaResposta = new ArrayList<>();

        for (CriarPedidoCommand.Item itemSolicitado : command.itens()) {
            validarProdutoDuplicado(
                itemSolicitado.produtoId(),
                produtosAdicionados
            );

            Produto produto = buscarProduto(
                itemSolicitado.produtoId(),
                command.unidadeId()
            );

            baixarEstoque(
                command.unidadeId(),
                produto.getId(),
                itemSolicitado.quantidade()
            );

            BigDecimal subtotal = produto
                .getPreco()
                .multiply(BigDecimal.valueOf(itemSolicitado.quantidade()));

            valorTotal = valorTotal.add(subtotal);

            itensParaPersistir.add(
                ItemPedido.builder()
                    .pedidoId(pedidoId)
                    .produtoId(produto.getId())
                    .quantidade(itemSolicitado.quantidade())
                    .precoUnitario(produto.getPreco())
                    .subtotal(subtotal)
                    .build()
            );

            itensDaResposta.add(
                new PedidoCriadoDTO.Item(
                    produto.getId(),
                    produto.getNome(),
                    itemSolicitado.quantidade(),
                    produto.getPreco(),
                    subtotal
                )
            );
        }

        Pedido pedido = new Pedido(
            pedidoId,
            command.clienteId(),
            command.unidadeId(),
            command.canalPedido(),
            valorTotal,
            StatusPedido.AGUARDANDO_PAGAMENTO
        );

        pedidoRepository.save(pedido);
        itemPedidoRepository.saveAll(itensParaPersistir);

        PedidoCriadoDTO resultado = new PedidoCriadoDTO(
            pedido.getId(),
            pedido.getClienteId(),
            pedido.getUnidadeId(),
            pedido.getCanalPedido(),
            pedido.getValorTotal(),
            pedido.getStatus(),
            List.copyOf(itensDaResposta)
        );

        salvarEventoOutbox(resultado);

        return resultado;
    }

    private void validarCliente(UUID clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException(
                "CLIENTE_NAO_ENCONTRADO",
                "Cliente não encontrado: " + clienteId
            );
        }
    }

    private void validarUnidade(UUID unidadeId) {
        if (!unidadeRepository.existsByIdAndAtivaTrue(unidadeId)) {
            throw new ResourceNotFoundException(
                "UNIDADE_NAO_ENCONTRADA",
                "Unidade ativa não encontrada: " + unidadeId
            );
        }
    }

    private void validarProdutoDuplicado(
        Long produtoId,
        Set<Long> produtosAdicionados
    ) {
        if (!produtosAdicionados.add(produtoId)) {
            throw new BusinessConflictException(
                "PRODUTO_DUPLICADO",
                "O produto " + produtoId
                    + " foi informado mais de uma vez"
            );
        }
    }

    private Produto buscarProduto(Long produtoId, UUID unidadeId) {
        return produtoRepository
            .findByIdAndUnidadeIdAndDisponivelTrue(produtoId, unidadeId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "PRODUTO_NAO_ENCONTRADO",
                "Produto disponível não encontrado na unidade: "
                    + produtoId
            ));
    }

    private void baixarEstoque(
        UUID unidadeId,
        Long produtoId,
        Integer quantidade
    ) {
        int registrosAtualizados = estoqueRepository.baixarEstoque(
            unidadeId,
            produtoId,
            quantidade
        );

        if (registrosAtualizados == 0) {
            throw new BusinessConflictException(
                "ESTOQUE_INSUFICIENTE",
                "Estoque insuficiente para o produto: " + produtoId
            );
        }
    }

    private void salvarEventoOutbox(PedidoCriadoDTO pedido) {
        try {
            String payload = objectMapper.writeValueAsString(pedido);

            OutboxMessage outbox = OutboxMessage.builder()
                .aggregateType("PEDIDO")
                .aggregateId(pedido.id().toString())
                .type("PEDIDO_CRIADO")
                .payload(payload)
                .build();

            outboxRepository.save(outbox);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                "Não foi possível serializar o evento do pedido",
                exception
            );
        }
    }
}