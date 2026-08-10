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

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "cardapio", key = "#unidadeId")
    public CardapioDTO buscarCardapioPorUnidade(Long unidadeId) {
        List<ProdutoDTO> itens = produtoRepository.findByUnidadeIdAndDisponivelTrue(unidadeId)
            .stream()
            .map(p -> new ProdutoDTO(
                p.getId(),
                p.getNome(),
                p.getDescricao(),
                p.getPreco(),
                p.getCategoria() != null ? p.getCategoria().getNome() : "Geral"
            ))
            .toList();

        return new CardapioDTO(unidadeId, itens);
    }

    @CacheEvict(value = "cardapio", key = "#unidadeId")
    public void limparCacheCardapio(Long unidadeId) {
        // Método utilitário para invalidar o cache quando um produto for atualizado
    }
}