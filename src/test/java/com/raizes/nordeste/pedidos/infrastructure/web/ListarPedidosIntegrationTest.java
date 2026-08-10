package com.raizes.nordeste.pedidos.infrastructure.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "cliente", roles = "CLIENTE")
class ListarPedidosIntegrationTest {

    private static final String CLIENTE_ID =
        "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";

    private static final String UNIDADE_RECIFE =
        "550e8400-e29b-41d4-a716-446655440000";

    private static final String UNIDADE_SALVADOR =
        "a1b2c3d4-e5f6-7890-abcd-ef1234567891";

    private static final UUID PEDIDO_1 =
        UUID.fromString(
            "10000000-0000-0000-0000-000000000001"
        );

    private static final UUID PEDIDO_2 =
        UUID.fromString(
            "10000000-0000-0000-0000-000000000002"
        );

    private static final UUID PEDIDO_3 =
        UUID.fromString(
            "10000000-0000-0000-0000-000000000003"
        );

    private static final UUID PEDIDO_4 =
        UUID.fromString(
            "10000000-0000-0000-0000-000000000004"
        );

    private static final UUID PEDIDO_5 =
        UUID.fromString(
            "10000000-0000-0000-0000-000000000005"
        );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {
        limparPedidos();
        inserirPedidos();
    }

    @AfterEach
    void limparBanco() {
        limparPedidos();
    }

    @Test
    void deveListarPedidosOrdenadosComPaginacaoPadrao()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.conteudo.length()")
                .value(5))
            .andExpect(jsonPath("$.conteudo[0].id")
                .value(PEDIDO_5.toString()))
            .andExpect(jsonPath("$.conteudo[1].id")
                .value(PEDIDO_4.toString()))
            .andExpect(jsonPath("$.conteudo[4].id")
                .value(PEDIDO_1.toString()))
            .andExpect(jsonPath("$.pagina")
                .value(0))
            .andExpect(jsonPath("$.tamanho")
                .value(20))
            .andExpect(jsonPath("$.totalElementos")
                .value(5))
            .andExpect(jsonPath("$.totalPaginas")
                .value(1))
            .andExpect(jsonPath("$.primeiraPagina")
                .value(true))
            .andExpect(jsonPath("$.ultimaPagina")
                .value(true));
    }

    @Test
    void deveFiltrarPedidosPorCanal()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos")
                    .param("canalPedido", "APP")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos")
                .value(2))
            .andExpect(jsonPath("$.conteudo.length()")
                .value(2))
            .andExpect(jsonPath("$.conteudo[0].id")
                .value(PEDIDO_3.toString()))
            .andExpect(jsonPath(
                "$.conteudo[0].canalPedido"
            ).value("APP"))
            .andExpect(jsonPath("$.conteudo[1].id")
                .value(PEDIDO_1.toString()))
            .andExpect(jsonPath(
                "$.conteudo[1].canalPedido"
            ).value("APP"));
    }

    @Test
    void deveFiltrarPedidosPorStatus()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos")
                    .param("status", "PAGO")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos")
                .value(2))
            .andExpect(jsonPath("$.conteudo.length()")
                .value(2))
            .andExpect(jsonPath("$.conteudo[0].id")
                .value(PEDIDO_3.toString()))
            .andExpect(jsonPath("$.conteudo[0].status")
                .value("PAGO"))
            .andExpect(jsonPath("$.conteudo[1].id")
                .value(PEDIDO_1.toString()))
            .andExpect(jsonPath("$.conteudo[1].status")
                .value("PAGO"));
    }

    @Test
    void deveFiltrarPedidosPorUnidade()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos")
                    .param(
                        "unidadeId",
                        UNIDADE_RECIFE
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos")
                .value(3))
            .andExpect(jsonPath("$.conteudo.length()")
                .value(3))
            .andExpect(jsonPath(
                "$.conteudo[0].unidadeId"
            ).value(UNIDADE_RECIFE))
            .andExpect(jsonPath(
                "$.conteudo[1].unidadeId"
            ).value(UNIDADE_RECIFE))
            .andExpect(jsonPath(
                "$.conteudo[2].unidadeId"
            ).value(UNIDADE_RECIFE));
    }

    @Test
    void deveCombinarFiltros()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos")
                    .param("canalPedido", "APP")
                    .param("status", "PAGO")
                    .param(
                        "unidadeId",
                        UNIDADE_SALVADOR
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos")
                .value(1))
            .andExpect(jsonPath("$.conteudo.length()")
                .value(1))
            .andExpect(jsonPath("$.conteudo[0].id")
                .value(PEDIDO_3.toString()))
            .andExpect(jsonPath(
                "$.conteudo[0].canalPedido"
            ).value("APP"))
            .andExpect(jsonPath("$.conteudo[0].status")
                .value("PAGO"))
            .andExpect(jsonPath(
                "$.conteudo[0].unidadeId"
            ).value(UNIDADE_SALVADOR));
    }

    @Test
    void deveNavegarEntrePaginas()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos")
                    .param("pagina", "1")
                    .param("tamanho", "2")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pagina")
                .value(1))
            .andExpect(jsonPath("$.tamanho")
                .value(2))
            .andExpect(jsonPath("$.totalElementos")
                .value(5))
            .andExpect(jsonPath("$.totalPaginas")
                .value(3))
            .andExpect(jsonPath("$.conteudo.length()")
                .value(2))
            .andExpect(jsonPath("$.conteudo[0].id")
                .value(PEDIDO_3.toString()))
            .andExpect(jsonPath("$.conteudo[1].id")
                .value(PEDIDO_2.toString()))
            .andExpect(jsonPath("$.primeiraPagina")
                .value(false))
            .andExpect(jsonPath("$.ultimaPagina")
                .value(false));
    }

    @Test
    void deveRecusarPaginaNegativa()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos")
                    .param("pagina", "-1")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("PAGINA_INVALIDA"));
    }

    @Test
    void deveRecusarTamanhoAcimaDoLimite()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos")
                    .param("tamanho", "101")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("TAMANHO_PAGINA_INVALIDO"));
    }

    @Test
    void deveRecusarCanalInvalido()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos")
                    .param(
                        "canalPedido",
                        "CANAL_INEXISTENTE"
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("INVALID_REQUEST"));
    }

    private void inserirPedidos() {
        inserirPedido(
            PEDIDO_1,
            UNIDADE_RECIFE,
            "APP",
            "PAGO",
            "2026-08-10 10:00:00-03",
            new BigDecimal("25.00")
        );

        inserirPedido(
            PEDIDO_2,
            UNIDADE_RECIFE,
            "TOTEM",
            "AGUARDANDO_PAGAMENTO",
            "2026-08-10 11:00:00-03",
            new BigDecimal("30.00")
        );

        inserirPedido(
            PEDIDO_3,
            UNIDADE_SALVADOR,
            "APP",
            "PAGO",
            "2026-08-10 12:00:00-03",
            new BigDecimal("40.00")
        );

        inserirPedido(
            PEDIDO_4,
            UNIDADE_RECIFE,
            "WEB",
            "PAGAMENTO_RECUSADO",
            "2026-08-10 13:00:00-03",
            new BigDecimal("50.00")
        );

        inserirPedido(
            PEDIDO_5,
            UNIDADE_SALVADOR,
            "BALCAO",
            "ENTREGUE",
            "2026-08-10 14:00:00-03",
            new BigDecimal("60.00")
        );
    }

    private void inserirPedido(
        UUID pedidoId,
        String unidadeId,
        String canalPedido,
        String statusPedido,
        String dataHora,
        BigDecimal valorTotal
    ) {
        jdbcTemplate.update(
            """
                INSERT INTO tb_pedidos (
                    id,
                    unidade_id,
                    cliente_id,
                    canal_pedido,
                    status,
                    valor_total,
                    data_hora
                )
                VALUES (
                    ?::uuid,
                    ?::uuid,
                    ?::uuid,
                    ?,
                    ?,
                    ?,
                    ?::timestamptz
                )
                """,
            pedidoId.toString(),
            unidadeId,
            CLIENTE_ID,
            canalPedido,
            statusPedido,
            valorTotal,
            dataHora
        );
    }

    private void limparPedidos() {
        jdbcTemplate.update(
            "DELETE FROM historico_pontos"
        );

        jdbcTemplate.update(
            "DELETE FROM tb_pagamentos"
        );

        jdbcTemplate.update(
            "DELETE FROM tb_pedido_itens"
        );

        jdbcTemplate.update(
            "DELETE FROM tb_outbox"
        );

        jdbcTemplate.update(
            "DELETE FROM tb_pedidos"
        );
    }
}