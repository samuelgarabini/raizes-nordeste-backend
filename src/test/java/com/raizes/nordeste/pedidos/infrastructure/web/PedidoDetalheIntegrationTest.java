package com.raizes.nordeste.pedidos.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "cliente", roles = "CLIENTE")
class PedidoDetalheIntegrationTest {

    private static final String CLIENTE_ID =
        "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";

    private static final String UNIDADE_RECIFE =
        "550e8400-e29b-41d4-a716-446655440000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {
        limparBanco();
    }

    @AfterEach
    void restaurarBanco() {
        limparBanco();
    }

    @Test
    void deveBuscarPedidoComItensAntesDoCheckout()
        throws Exception {

        UUID pedidoId = criarPedidoPadrao();

        mockMvc.perform(
                get(
                    "/api/v1/pedidos/{id}",
                    pedidoId
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id")
                .value(pedidoId.toString()))
            .andExpect(jsonPath("$.clienteId")
                .value(CLIENTE_ID))
            .andExpect(jsonPath("$.unidadeId")
                .value(UNIDADE_RECIFE))
            .andExpect(jsonPath("$.canalPedido")
                .value("APP"))
            .andExpect(jsonPath("$.valorTotal")
                .value(58.3))
            .andExpect(jsonPath("$.status")
                .value("AGUARDANDO_PAGAMENTO"))
            .andExpect(jsonPath("$.dataHora")
                .isNotEmpty())
            .andExpect(jsonPath("$.itens.length()")
                .value(2))
            .andExpect(jsonPath("$.itens[0].produtoId")
                .value(101))
            .andExpect(jsonPath("$.itens[0].quantidade")
                .value(2))
            .andExpect(jsonPath(
                "$.itens[0].precoUnitario"
            ).value(24.9))
            .andExpect(jsonPath("$.itens[0].subtotal")
                .value(49.8))
            .andExpect(jsonPath("$.itens[1].produtoId")
                .value(104))
            .andExpect(jsonPath("$.itens[1].quantidade")
                .value(1))
            .andExpect(jsonPath(
                "$.itens[1].precoUnitario"
            ).value(8.5))
            .andExpect(jsonPath("$.itens[1].subtotal")
                .value(8.5))
            .andExpect(jsonPath("$.pagamento")
                .doesNotExist());
    }

    @Test
    void deveBuscarPedidoComPagamentoAprovado()
        throws Exception {

        UUID pedidoId = criarPedidoPadrao();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
                    .param(
                        "resultadoPagamento",
                        "APROVADO"
                    )
            )
            .andExpect(status().isOk());

        mockMvc.perform(
                get(
                    "/api/v1/pedidos/{id}",
                    pedidoId
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id")
                .value(pedidoId.toString()))
            .andExpect(jsonPath("$.status")
                .value("PAGO"))
            .andExpect(jsonPath("$.valorTotal")
                .value(58.3))
            .andExpect(jsonPath("$.itens.length()")
                .value(2))
            .andExpect(jsonPath(
                "$.pagamento.transacaoId"
            ).isNotEmpty())
            .andExpect(jsonPath("$.pagamento.status")
                .value("APROVADO"))
            .andExpect(jsonPath("$.pagamento.valor")
                .value(58.3))
            .andExpect(jsonPath("$.pagamento.gateway")
                .value("MOCK_GATEWAY"))
            .andExpect(jsonPath(
                "$.pagamento.processadoEm"
            ).isNotEmpty())
            .andExpect(jsonPath(
                "$.pagamento.motivoRecusa"
            ).doesNotExist());
    }

    @Test
    void deveBuscarPedidoComPagamentoRecusado()
        throws Exception {

        UUID pedidoId = criarPedidoPadrao();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
                    .param(
                        "resultadoPagamento",
                        "RECUSADO"
                    )
            )
            .andExpect(status().isOk());

        mockMvc.perform(
                get(
                    "/api/v1/pedidos/{id}",
                    pedidoId
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id")
                .value(pedidoId.toString()))
            .andExpect(jsonPath("$.status")
                .value("PAGAMENTO_RECUSADO"))
            .andExpect(jsonPath("$.itens.length()")
                .value(2))
            .andExpect(jsonPath(
                "$.pagamento.transacaoId"
            ).isNotEmpty())
            .andExpect(jsonPath("$.pagamento.status")
                .value("RECUSADO"))
            .andExpect(jsonPath("$.pagamento.valor")
                .value(58.3))
            .andExpect(jsonPath("$.pagamento.gateway")
                .value("MOCK_GATEWAY"))
            .andExpect(jsonPath(
                "$.pagamento.motivoRecusa"
            ).value(
                "Pagamento recusado pelo gateway mock"
            ))
            .andExpect(jsonPath(
                "$.pagamento.processadoEm"
            ).isNotEmpty());
    }

    @Test
    void deveRetornar404ParaPedidoInexistente()
        throws Exception {

        UUID pedidoId = UUID.randomUUID();

        mockMvc.perform(
                get(
                    "/api/v1/pedidos/{id}",
                    pedidoId
                )
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("PEDIDO_NAO_ENCONTRADO"))
            .andExpect(jsonPath("$.message")
                .value(
                    "Pedido não encontrado: "
                        + pedidoId
                ));
    }

    private UUID criarPedidoPadrao()
        throws Exception {

        String request = """
            {
              "clienteId": "%s",
              "unidadeId": "%s",
              "canalPedido": "APP",
              "itens": [
                {
                  "produtoId": 101,
                  "quantidade": 2
                },
                {
                  "produtoId": 104,
                  "quantidade": 1
                }
              ]
            }
            """.formatted(
                CLIENTE_ID,
                UNIDADE_RECIFE
            );

        MvcResult result = mockMvc.perform(
                post("/api/v1/pedidos")
                    .contentType("application/json")
                    .content(request)
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode response = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        return UUID.fromString(
            response.get("id").asText()
        );
    }

    private void limparBanco() {
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

        jdbcTemplate.update(
            """
                INSERT INTO carteiras_fidelidade (
                    cliente_id,
                    pontos_acumulados,
                    ultima_atualizacao
                )
                VALUES (
                    ?::uuid,
                    50,
                    CURRENT_TIMESTAMP
                )
                ON CONFLICT (cliente_id)
                DO UPDATE SET
                    pontos_acumulados = 50,
                    ultima_atualizacao =
                        CURRENT_TIMESTAMP
                """,
            CLIENTE_ID
        );

        jdbcTemplate.update(
            """
                UPDATE tb_estoques estoque
                SET quantidade = CASE
                        WHEN produto.disponivel = TRUE
                            THEN 50
                        ELSE 0
                    END,
                    atualizado_em =
                        CURRENT_TIMESTAMP
                FROM tb_produtos produto
                WHERE produto.id =
                    estoque.produto_id
                """
        );
    }
}