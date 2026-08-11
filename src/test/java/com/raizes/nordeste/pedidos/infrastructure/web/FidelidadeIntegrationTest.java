package com.raizes.nordeste.pedidos.infrastructure.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FidelidadeIntegrationTest {

    private static final String CLIENTE_ID =
        "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";

    @Autowired
    private MockMvc mockMvc;

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
    void deveConsultarSaldoConsentimentoEHistorico()
        throws Exception {

        mockMvc.perform(
                get(
                    "/api/v1/fidelidade/{clienteId}",
                    CLIENTE_ID
                )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clienteId")
                .value(CLIENTE_ID))
            .andExpect(jsonPath("$.saldoPontos")
                .value(50))
            .andExpect(jsonPath("$.consentimento.concedido")
                .value(true))
            .andExpect(jsonPath("$.consentimento.finalidade")
                .value("PROGRAMA_FIDELIDADE"))
            .andExpect(jsonPath("$.consentimento.baseLegal")
                .value("CONSENTIMENTO"))
            .andExpect(jsonPath("$.historico")
                .isEmpty());

        validarAuditoria(
            "CONSULTA_FIDELIDADE",
            "SUCESSO",
            null
        );
    }

    @Test
    void deveRevogarConsentimento()
        throws Exception {

        mockMvc.perform(
                put(
                    "/api/v1/fidelidade/{clienteId}/consentimento",
                    CLIENTE_ID
                )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "concedido": false,
                          "versaoTermo": "1.0"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.concedido")
                .value(false))
            .andExpect(jsonPath("$.revogadoEm")
                .isNotEmpty());

        assertThat(consentimentoAtivo())
            .isFalse();

        validarAuditoria(
            "CONSENTIMENTO_FIDELIDADE_REVOGADO",
            "SUCESSO",
            null
        );
    }

    @Test
    void deveConcederConsentimentoNovamente()
        throws Exception {

        revogarConsentimentoDiretamente();

        mockMvc.perform(
                put(
                    "/api/v1/fidelidade/{clienteId}/consentimento",
                    CLIENTE_ID
                )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "concedido": true,
                          "versaoTermo": "1.1"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.concedido")
                .value(true))
            .andExpect(jsonPath("$.versaoTermo")
                .value("1.1"))
            .andExpect(jsonPath("$.concedidoEm")
                .isNotEmpty())
            .andExpect(jsonPath("$.revogadoEm")
                .doesNotExist());

        assertThat(consentimentoAtivo())
            .isTrue();

        validarAuditoria(
            "CONSENTIMENTO_FIDELIDADE_CONCEDIDO",
            "SUCESSO",
            null
        );
    }

    @Test
    void deveResgatarPontosERegistrarDebito()
        throws Exception {

        mockMvc.perform(
                post(
                    "/api/v1/fidelidade/{clienteId}/resgates",
                    CLIENTE_ID
                )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "pontos": 10
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.operacaoId")
                .isNotEmpty())
            .andExpect(jsonPath("$.clienteId")
                .value(CLIENTE_ID))
            .andExpect(jsonPath("$.pontosResgatados")
                .value(10))
            .andExpect(jsonPath("$.saldoAnterior")
                .value(50))
            .andExpect(jsonPath("$.saldoAtual")
                .value(40))
            .andExpect(jsonPath("$.resgatadoEm")
                .isNotEmpty());

        assertThat(saldoPontos())
            .isEqualTo(40);

        Map<String, Object> historico =
            jdbcTemplate.queryForMap(
                """
                    SELECT
                        operacao_id,
                        pedido_id,
                        pontos_alterados,
                        tipo_operacao
                    FROM historico_pontos
                    """
            );

        assertThat(historico.get("operacao_id"))
            .isNotNull();
        assertThat(historico.get("pedido_id"))
            .isNull();
        assertThat(historico.get("pontos_alterados"))
            .isEqualTo(10);
        assertThat(historico.get("tipo_operacao"))
            .isEqualTo("DEBITO");

        validarAuditoria(
            "RESGATE_FIDELIDADE",
            "SUCESSO",
            null
        );
    }

    @Test
    void deveRecusarResgateComSaldoInsuficiente()
        throws Exception {

        mockMvc.perform(
                post(
                    "/api/v1/fidelidade/{clienteId}/resgates",
                    CLIENTE_ID
                )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "pontos": 51
                        }
                        """)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("SALDO_PONTOS_INSUFICIENTE"));

        assertThat(saldoPontos())
            .isEqualTo(50);
        assertThat(quantidadeHistoricos())
            .isZero();

        validarAuditoria(
            "RESGATE_FIDELIDADE",
            "FALHA",
            "SALDO_PONTOS_INSUFICIENTE"
        );
    }

    @Test
    void deveRecusarResgateSemConsentimento()
        throws Exception {

        revogarConsentimentoDiretamente();

        mockMvc.perform(
                post(
                    "/api/v1/fidelidade/{clienteId}/resgates",
                    CLIENTE_ID
                )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "pontos": 10
                        }
                        """)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value(
                    "CONSENTIMENTO_FIDELIDADE_NECESSARIO"
                ));

        assertThat(saldoPontos())
            .isEqualTo(50);
        assertThat(quantidadeHistoricos())
            .isZero();

        validarAuditoria(
            "RESGATE_FIDELIDADE",
            "FALHA",
            "CONSENTIMENTO_FIDELIDADE_NECESSARIO"
        );
    }

    @Test
    void deveValidarQuantidadeDoResgate()
        throws Exception {

        mockMvc.perform(
                post(
                    "/api/v1/fidelidade/{clienteId}/resgates",
                    CLIENTE_ID
                )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "pontos": 0
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("VALIDATION_ERROR"));

        assertThat(saldoPontos())
            .isEqualTo(50);
    }

    @Test
    void clienteNaoDeveAcessarOutraCarteira()
        throws Exception {

        UUID outroClienteId = UUID.randomUUID();

        mockMvc.perform(
                get(
                    "/api/v1/fidelidade/{clienteId}",
                    outroClienteId
                )
                    .with(
                        user("cliente")
                            .roles("CLIENTE")
                    )
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error")
                .value("ACESSO_CLIENTE_NEGADO"));

        validarAuditoriaPorRecurso(
            "CONSULTA_FIDELIDADE",
            "FALHA",
            outroClienteId,
            "ACESSO_CLIENTE_NEGADO"
        );
    }

    @Test
    void equipeOperacionalPodeConsultarCarteira()
        throws Exception {

        mockMvc.perform(
                get(
                    "/api/v1/fidelidade/{clienteId}",
                    CLIENTE_ID
                )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.saldoPontos")
                .value(50));
    }

    @Test
    void atendenteNaoDeveAlterarConsentimento()
        throws Exception {

        mockMvc.perform(
                put(
                    "/api/v1/fidelidade/{clienteId}/consentimento",
                    CLIENTE_ID
                )
                    .with(
                        user("atendente")
                            .roles("ATENDENTE")
                    )
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "concedido": false,
                          "versaoTermo": "1.0"
                        }
                        """)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error")
                .value("ACESSO_NEGADO"));

        assertThat(consentimentoAtivo())
            .isTrue();
    }

    @Test
    void deveExigirAutenticacaoParaConsultar()
        throws Exception {

        mockMvc.perform(
                get(
                    "/api/v1/fidelidade/{clienteId}",
                    CLIENTE_ID
                )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("NAO_AUTENTICADO"));
    }

    private Integer saldoPontos() {
        return jdbcTemplate.queryForObject(
            """
                SELECT pontos_acumulados
                FROM carteiras_fidelidade
                WHERE cliente_id = ?::uuid
                """,
            Integer.class,
            CLIENTE_ID
        );
    }

    private Boolean consentimentoAtivo() {
        return jdbcTemplate.queryForObject(
            """
                SELECT concedido
                FROM consentimentos_fidelidade
                WHERE cliente_id = ?::uuid
                """,
            Boolean.class,
            CLIENTE_ID
        );
    }

    private Long quantidadeHistoricos() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM historico_pontos",
            Long.class
        );
    }

    private void revogarConsentimentoDiretamente() {
        jdbcTemplate.update(
            """
                UPDATE consentimentos_fidelidade
                SET concedido = FALSE,
                    revogado_em = CURRENT_TIMESTAMP,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE cliente_id = ?::uuid
                """,
            CLIENTE_ID
        );
    }

    private void validarAuditoria(
        String evento,
        String resultado,
        String codigoErro
    ) {
        validarAuditoriaPorRecurso(
            evento,
            resultado,
            UUID.fromString(CLIENTE_ID),
            codigoErro
        );
    }

    private void validarAuditoriaPorRecurso(
        String evento,
        String resultado,
        UUID recursoId,
        String codigoErro
    ) {
        Map<String, Object> audit =
            jdbcTemplate.queryForMap(
                """
                    SELECT
                        evento,
                        resultado,
                        ator_fingerprint,
                        perfil,
                        recurso,
                        recurso_id,
                        codigo_erro
                    FROM tb_auditoria_seguranca
                    WHERE evento = ?
                      AND resultado = ?
                      AND recurso_id = ?
                    ORDER BY ocorrido_em DESC
                    LIMIT 1
                    """,
                evento,
                resultado,
                recursoId.toString()
            );

        assertThat(audit.get("evento"))
            .isEqualTo(evento);
        assertThat(audit.get("resultado"))
            .isEqualTo(resultado);
        assertThat(audit.get("ator_fingerprint"))
            .asString()
            .hasSize(64)
            .doesNotContain("cliente");
        assertThat(audit.get("perfil"))
            .isEqualTo("CLIENTE");
        assertThat(audit.get("recurso"))
            .isEqualTo("CLIENTE");
        assertThat(audit.get("recurso_id"))
            .isEqualTo(recursoId.toString());
        assertThat(audit.get("codigo_erro"))
            .isEqualTo(codigoErro);
    }

    private void limparBanco() {
        jdbcTemplate.update(
            "DELETE FROM tb_auditoria_seguranca"
        );
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
                    ultima_atualizacao = CURRENT_TIMESTAMP
                """,
            CLIENTE_ID
        );

        jdbcTemplate.update(
            """
                INSERT INTO consentimentos_fidelidade (
                    cliente_id,
                    concedido,
                    versao_termo,
                    concedido_em,
                    revogado_em,
                    atualizado_em
                )
                VALUES (
                    ?::uuid,
                    TRUE,
                    '1.0',
                    CURRENT_TIMESTAMP,
                    NULL,
                    CURRENT_TIMESTAMP
                )
                ON CONFLICT (cliente_id)
                DO UPDATE SET
                    concedido = TRUE,
                    versao_termo = '1.0',
                    concedido_em = CURRENT_TIMESTAMP,
                    revogado_em = NULL,
                    atualizado_em = CURRENT_TIMESTAMP
                """,
            CLIENTE_ID
        );
    }
}
