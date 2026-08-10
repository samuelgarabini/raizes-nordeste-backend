package com.raizes.nordeste.pedidos.application;

import com.raizes.nordeste.pedidos.application.dto.FiltroPedidosCommand;
import com.raizes.nordeste.pedidos.application.dto.PaginaResponseDTO;
import com.raizes.nordeste.pedidos.application.dto.PedidoResumoDTO;
import com.raizes.nordeste.pedidos.infrastructure.exception.InvalidRequestException;
import com.raizes.nordeste.pedidos.repository.PedidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuscarTodosPedidosUseCase {

    private static final int TAMANHO_MAXIMO = 100;

    private final PedidoRepository pedidoRepository;

    public BuscarTodosPedidosUseCase(
        PedidoRepository pedidoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<PedidoResumoDTO> executar(
        FiltroPedidosCommand filtros
    ) {
        validarFiltros(filtros);

        Sort ordenacao = Sort.by(
            Sort.Order.desc("dataHora"),
            Sort.Order.desc("id")
        );

        Pageable pageable = PageRequest.of(
            filtros.pagina(),
            filtros.tamanho(),
            ordenacao
        );

        Page<PedidoResumoDTO> resultado =
            pedidoRepository.buscarComFiltros(
                filtros.canalPedido(),
                filtros.status(),
                filtros.unidadeId(),
                pageable
            )
            .map(PedidoResumoDTO::de);

        return PaginaResponseDTO.de(resultado);
    }

    private void validarFiltros(
        FiltroPedidosCommand filtros
    ) {
        if (filtros == null) {
            throw new InvalidRequestException(
                "FILTROS_INVALIDOS",
                "Os filtros da consulta não podem ser nulos"
            );
        }

        if (filtros.pagina() < 0) {
            throw new InvalidRequestException(
                "PAGINA_INVALIDA",
                "O número da página deve ser maior "
                    + "ou igual a zero"
            );
        }

        if (
            filtros.tamanho() < 1
                || filtros.tamanho() > TAMANHO_MAXIMO
        ) {
            throw new InvalidRequestException(
                "TAMANHO_PAGINA_INVALIDO",
                "O tamanho da página deve estar "
                    + "entre 1 e "
                    + TAMANHO_MAXIMO
            );
        }
    }
}