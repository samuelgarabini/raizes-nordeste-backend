package com.raizes.nordeste.pedidos.infrastructure.web;

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
import org.springframework.test.web.servlet.ResultActions;
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
class CancelarPedidoIntegrationTest {

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
    void deveCancelarPedidoEDevolverEstoque()
        throws Exception {

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(50);

        UUID pedidoId = criarPedido();

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(49);

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/cancelamento",
                    pedidoId
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
                .value("AGUARDANDO_PAGAMENTO"))
            .andExpect(jsonPath("$.statusAtual")
                .value("CANCELADO"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("CANCELADO");

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(50);

        Map<String, Object> audit =
            buscarAuditoria(
                pedidoId,
                "SUCESSO"
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
    void deveImpedirCancelamentoDuplicado()
        throws Exception {

        UUID pedidoId = criarPedido();

        cancelarComoAtendente(pedidoId)
            .andExpect(status().isOk());

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(50);

        cancelarComoAtendente(pedidoId)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value(
                    "CANCELAMENTO_NAO_PERMITIDO"
                ));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("CANCELADO");

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(50);

        Map<String, Object> audit =
            buscarAuditoria(
                pedidoId,
                "FALHA"
            );

        validarAuditoriaBase(
            audit,
            pedidoId,
            "FALHA"
        );

        assertThat(audit.get("codigo_erro"))
            .isEqualTo(
                "CANCELAMENTO_NAO_PERMITIDO"
            );
    }

    @Test
    void deveRecusarCancelamentoDePedidoPago()
        throws Exception {

        UUID pedidoId = criarPedidoPago();

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(49);

        limparAuditoria();

        cancelarComoAtendente(pedidoId)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value(
                    "CANCELAMENTO_NAO_PERMITIDO"
                ));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("PAGO");

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(49);

        Map<String, Object> audit =
            buscarAuditoria(
                pedidoId,
                "FALHA"
            );

        assertThat(audit.get("codigo_erro"))
            .isEqualTo(
                "CANCELAMENTO_NAO_PERMITIDO"
            );
    }

    @Test
    void deveRetornar404ParaPedidoInexistente()
        throws Exception {

        UUID pedidoId = UUID.randomUUID();

        cancelarComoAtendente(pedidoId)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("PEDIDO_NAO_ENCONTRADO"));

        Map<String, Object> audit =
            buscarAuditoria(
                pedidoId,
                "FALHA"
            );

        assertThat(audit.get("codigo_erro"))
            .isEqualTo("PEDIDO_NAO_ENCONTRADO");
    }

    @Test
    void deveProibirCancelamentoPorCliente()
        throws Exception {

        UUID pedidoId = criarPedido();

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/cancelamento",
                    pedidoId
                )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error")
                .value("ACESSO_NEGADO"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("AGUARDANDO_PAGAMENTO");

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(49);

        assertThat(
            quantidadeAuditoriasCancelamento()
        ).isZero();
    }

    @Test
    void deveExigirAutenticacaoParaCancelar()
        throws Exception {

        UUID pedidoId = criarPedido();

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/cancelamento",
                    pedidoId
                )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("NAO_AUTENTICADO"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("AGUARDANDO_PAGAMENTO");

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(49);

        assertThat(
            quantidadeAuditoriasCancelamento()
        ).isZero();
    }

    private ResultActions cancelarComoAtendente(
            UUID pedidoId
        ) throws Exception {

        return mockMvc.perform(
            patch(
                "/api/v1/pedidos/{id}/cancelamento",
                pedidoId
            )
                .with(
                    user("atendente")
                        .roles("ATENDENTE")
                )
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

    private Integer estoqueDoProduto(
        Long produtoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT quantidade
                FROM tb_estoques
                WHERE unidade_id = ?::uuid
                  AND produto_id = ?
                """,
            Integer.class,
            UNIDADE_RECIFE,
            produtoId
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

    private Map<String, Object> buscarAuditoria(
        UUID pedidoId,
        String resultado
    ) {
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
                WHERE evento =
                    'CANCELAMENTO_PEDIDO'
                  AND recurso_id = ?
                  AND resultado = ?
                ORDER BY ocorrido_em DESC
                LIMIT 1
                """,
            pedidoId.toString(),
            resultado
        );
    }

    private void validarAuditoriaBase(
        Map<String, Object> audit,
        UUID pedidoId,
        String resultado
    ) {
        assertThat(audit.get("evento"))
            .isEqualTo("CANCELAMENTO_PEDIDO");

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

    private Long quantidadeAuditoriasCancelamento() {
        return jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM tb_auditoria_seguranca
                WHERE evento =
                    'CANCELAMENTO_PEDIDO'
                """,
            Long.class
        );
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
