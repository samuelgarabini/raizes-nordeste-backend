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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AtualizarStatusPedidoIntegrationTest {

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
    void deveExecutarCicloOperacionalCompleto()
        throws Exception {

        UUID pedidoId = criarPedidoPago();

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    pedidoId
                )
                    .param(
                        "novoStatus",
                        "EM_PREPARACAO"
                    )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pedidoId")
                .value(pedidoId.toString()))
            .andExpect(jsonPath("$.statusAnterior")
                .value("PAGO"))
            .andExpect(jsonPath("$.statusAtual")
                .value("EM_PREPARACAO"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("EM_PREPARACAO");

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    pedidoId
                )
                    .param(
                        "novoStatus",
                        "PRONTO"
                    )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusAnterior")
                .value("EM_PREPARACAO"))
            .andExpect(jsonPath("$.statusAtual")
                .value("PRONTO"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("PRONTO");

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    pedidoId
                )
                    .param(
                        "novoStatus",
                        "ENTREGUE"
                    )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusAnterior")
                .value("PRONTO"))
            .andExpect(jsonPath("$.statusAtual")
                .value("ENTREGUE"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("ENTREGUE");
    }

    @Test
    void deveRecusarSaltoDeStatus()
        throws Exception {

        UUID pedidoId = criarPedidoPago();

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    pedidoId
                )
                    .param(
                        "novoStatus",
                        "PRONTO"
                    )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("TRANSICAO_STATUS_INVALIDA"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("PAGO");
    }

    @Test
    void deveRecusarPreparacaoAntesDoPagamento()
        throws Exception {

        UUID pedidoId = criarPedido();

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    pedidoId
                )
                    .param(
                        "novoStatus",
                        "EM_PREPARACAO"
                    )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("TRANSICAO_STATUS_INVALIDA"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("AGUARDANDO_PAGAMENTO");
    }

    @Test
    void deveRetornar404ParaPedidoInexistente()
        throws Exception {

        UUID pedidoId = UUID.randomUUID();

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    pedidoId
                )
                    .param(
                        "novoStatus",
                        "EM_PREPARACAO"
                    )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("PEDIDO_NAO_ENCONTRADO"));
    }

    @Test
    void deveRecusarStatusNaoInformado()
        throws Exception {

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    UUID.randomUUID()
                )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("NOVO_STATUS_INVALIDO"));
    }

    @Test
    void deveRecusarTextoDeStatusInvalido()
        throws Exception {

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    UUID.randomUUID()
                )
                    .param(
                        "novoStatus",
                        "STATUS_INEXISTENTE"
                    )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("INVALID_REQUEST"));
    }

    @Test
    void deveProibirAlteracaoDeStatusPorCliente()
        throws Exception {

        UUID pedidoId = criarPedidoPago();

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    pedidoId
                )
                    .param(
                        "novoStatus",
                        "EM_PREPARACAO"
                    )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
            )
            .andExpect(status().isForbidden());

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("PAGO");
    }

    private UUID criarPedidoPago()
        throws Exception {

        UUID pedidoId = criarPedido();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
                    .param(
                        "resultadoPagamento",
                        "APROVADO"
                    )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status")
                .value("PAGO"));

        return pedidoId;
    }

    private UUID criarPedido()
        throws Exception {

        String request = """
            {
              "clienteId": "%s",
              "unidadeId": "%s",
              "canalPedido": "APP",
              "itens": [
                {
                  "produtoId": 101,
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
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
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

    private String statusDoPedido(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT status
                FROM tb_pedidos
                WHERE id = ?
                """,
            String.class,
            pedidoId
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