package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.infrastructure.repository.ProdutoRepository;
import com.raizes.nordeste.pedidos.presentation.dto.CardapioDTO;
import com.raizes.nordeste.pedidos.presentation.dto.ProdutoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "cardapio", key = "#unidadeId")
    public CardapioDTO buscarCardapioPorUnidade(UUID unidadeId) {
        List<ProdutoDTO> itens = produtoRepository
            .findByUnidadeIdAndDisponivelTrue(unidadeId)
            .stream()
            .map(produto -> new ProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria() != null
                    ? produto.getCategoria().getNome()
                    : "Geral"
            ))
            .toList();

        return new CardapioDTO(unidadeId, itens);
    }

    @CacheEvict(value = "cardapio", key = "#unidadeId")
    public void limparCacheCardapio(UUID unidadeId) {
        // A anotação @CacheEvict realiza a invalidação do cache.
    }
}