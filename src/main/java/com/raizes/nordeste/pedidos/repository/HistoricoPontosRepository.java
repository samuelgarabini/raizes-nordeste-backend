package com.raizes.nordeste.pedidos.repository;

import com.raizes.nordeste.pedidos.domain.HistoricoPontos;
import com.raizes.nordeste.pedidos.domain.TipoOperacaoPontos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoricoPontosRepository
    extends JpaRepository<HistoricoPontos, Long> {

    boolean existsByPedidoIdAndTipoOperacao(
        UUID pedidoId,
        TipoOperacaoPontos tipoOperacao
    );

    List<HistoricoPontos>
        findByCarteiraIdOrderByDataOperacaoDesc(
            Long carteiraId
        );
}
