package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.entity.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByUnidadeIdAndProdutoId(
        UUID unidadeId,
        Long produtoId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        value = """
            UPDATE tb_estoques
            SET quantidade = quantidade - :quantidade,
                atualizado_em = CURRENT_TIMESTAMP
            WHERE unidade_id = :unidadeId
              AND produto_id = :produtoId
              AND quantidade >= :quantidade
            """,
        nativeQuery = true
    )
    int baixarEstoque(
        @Param("unidadeId") UUID unidadeId,
        @Param("produtoId") Long produtoId,
        @Param("quantidade") Integer quantidade
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        value = """
            UPDATE tb_estoques
            SET quantidade = quantidade + :quantidade,
                atualizado_em = CURRENT_TIMESTAMP
            WHERE unidade_id = :unidadeId
              AND produto_id = :produtoId
            """,
        nativeQuery = true
    )
    int devolverEstoque(
        @Param("unidadeId") UUID unidadeId,
        @Param("produtoId") Long produtoId,
        @Param("quantidade") Integer quantidade
    );
}