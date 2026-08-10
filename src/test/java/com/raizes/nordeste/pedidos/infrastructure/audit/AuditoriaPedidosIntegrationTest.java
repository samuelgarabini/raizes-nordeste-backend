package com.raizes.nordeste.pedidos.infrastructure.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditoriaPedidosIntegrationTest {

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
    void deveAuditarCheckoutComSucesso()
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

        Map<String, Object> audit =
            buscarUltimaAuditoria(
                "CHECKOUT_PEDIDO"
            );

        validarAuditoriaBase(
            audit,
            pedidoId,
            "SUCESSO"
        );

        assertThat(audit.get("codigo_erro"))
            .isNull();
    }

    @Test
    void deveAuditarFalhaDeCheckout()
        throws Exception {

        UUID pedidoId = criarPedido();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
                    .param(
                        "codigoPromocional",
                        "CUPOM_INEXISTENTE"
                    )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("CUPOM_INVALIDO"));

        Map<String, Object> audit =
            buscarUltimaAuditoria(
                "CHECKOUT_PEDIDO"
            );

        validarAuditoriaBase(
            audit,
            pedidoId,
            "FALHA"
        );

        assertThat(audit.get("codigo_erro"))
            .isEqualTo("CUPOM_INVALIDO");
    }

    @Test
    void deveAuditarAlteracaoDeStatus()
        throws Exception {

        UUID pedidoId = criarPedidoPago();

        limparAuditoria();

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
            .andExpect(jsonPath("$.statusAtual")
                .value("EM_PREPARACAO"));

        Map<String, Object> audit =
            buscarUltimaAuditoria(
                "ALTERACAO_STATUS_PEDIDO"
            );

        validarAuditoriaBase(
            audit,
            pedidoId,
            "SUCESSO"
        );

        assertThat(audit.get("codigo_erro"))
            .isNull();
    }

    @Test
    void deveAuditarTransicaoDeStatusInvalida()
        throws Exception {

        UUID pedidoId = criarPedidoPago();

        limparAuditoria();

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

        Map<String, Object> audit =
            buscarUltimaAuditoria(
                "ALTERACAO_STATUS_PEDIDO"
            );

        validarAuditoriaBase(
            audit,
            pedidoId,
            "FALHA"
        );

        assertThat(audit.get("codigo_erro"))
            .isEqualTo(
                "TRANSICAO_STATUS_INVALIDA"
            );
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
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content(request)
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode response = objectMapper.readTree(
            result
                .getResponse()
                .getContentAsString()
        );

        return UUID.fromString(
            response.get("id").asText()
        );
    }

    private Map<String, Object>
        buscarUltimaAuditoria(String evento) {

        return jdbcTemplate.queryForMap(
            """
                SELECT
                    evento,
                    resultado,
                    ator_fingerprint,
                    perfil,
                    recurso,
                    recurso_id,
                    ip_fingerprint,
                    codigo_erro
                FROM tb_auditoria_seguranca
                WHERE evento = ?
                ORDER BY ocorrido_em DESC
                LIMIT 1
                """,
            evento
        );
    }

    private void validarAuditoriaBase(
        Map<String, Object> audit,
        UUID pedidoId,
        String resultado
    ) {
        assertThat(audit.get("resultado"))
            .isEqualTo(resultado);

        assertThat(audit.get("perfil"))
            .isEqualTo("ATENDENTE");

        assertThat(audit.get("recurso"))
            .isEqualTo("PEDIDO");

        assertThat(audit.get("recurso_id"))
            .isEqualTo(pedidoId.toString());

        assertThat(
            audit.get("ator_fingerprint")
        )
            .asString()
            .hasSize(64)
            .doesNotContain("atendente");

        assertThat(
            audit.get("ip_fingerprint")
        )
            .asString()
            .hasSize(64);
    }

    private void limparAuditoria() {
        jdbcTemplate.update(
            "DELETE FROM tb_auditoria_seguranca"
        );
    }

    private void limparBanco() {
        limparAuditoria();

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
                UPDATE campanhas
                SET ativo = TRUE,
                    data_inicio =
                        CURRENT_TIMESTAMP
                        - INTERVAL '1 day',
                    data_fim =
                        CURRENT_TIMESTAMP
                        + INTERVAL '30 days'
                WHERE codigo_promocional =
                    'BEMVINDO10'
                """
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