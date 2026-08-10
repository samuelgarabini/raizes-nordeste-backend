package com.raizes.nordeste.pedidos.infrastructure.repository;

import com.raizes.nordeste.pedidos.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByUnidadeIdAndDisponivelTrue(UUID unidadeId);
}