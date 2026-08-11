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

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "cliente", roles = "CLIENTE")
class CheckoutIntegrationTest {

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
    void deveProcessarCheckoutApenasUmaVez()
        throws Exception {

        UUID pedidoId = criarPedidoPadrao();

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(48);

        assertThat(estoqueDoProduto(104L))
            .isEqualTo(49);

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status")
                .value("PAGO"))
            .andExpect(jsonPath("$.valorTotal")
                .value(58.3))
            .andExpect(jsonPath("$.statusPagamento")
                .value("APROVADO"))
            .andExpect(jsonPath("$.gateway")
                .value("MOCK_GATEWAY"))
            .andExpect(jsonPath("$.transacaoId")
                .isNotEmpty())
            .andExpect(jsonPath("$.processadoEm")
                .isNotEmpty());

        assertThat(pontosDoCliente())
            .isEqualTo(55);

        assertCreditoRegistrado(
            pedidoId,
            5
        );

        assertPagamentoAprovado(
            pedidoId,
            new BigDecimal("58.30")
        );

        /*
         * Pagamento aprovado não devolve os produtos.
         */
        assertThat(estoqueDoProduto(101L))
            .isEqualTo(48);

        assertThat(estoqueDoProduto(104L))
            .isEqualTo(49);

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("CHECKOUT_NAO_PERMITIDO"));

        assertThat(pontosDoCliente())
            .isEqualTo(55);

        assertCreditoRegistrado(
            pedidoId,
            5
        );

        assertPagamentoAprovado(
            pedidoId,
            new BigDecimal("58.30")
        );
    }

    @Test
    void deveRecusarPagamentoEDevolverEstoque()
        throws Exception {

        assertThat(estoqueDoProduto(101L))
            .isEqualTo(50);

        assertThat(estoqueDoProduto(104L))
            .isEqualTo(50);

        UUID pedidoId = criarPedidoPadrao();

        /*
         * A criação do pedido reserva os produtos.
         */
        assertThat(estoqueDoProduto(101L))
            .isEqualTo(48);

        assertThat(estoqueDoProduto(104L))
            .isEqualTo(49);

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
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status")
                .value("PAGAMENTO_RECUSADO"))
            .andExpect(jsonPath("$.valorTotal")
                .value(58.3))
            .andExpect(jsonPath("$.statusPagamento")
                .value("RECUSADO"))
            .andExpect(jsonPath("$.gateway")
                .value("MOCK_GATEWAY"))
            .andExpect(jsonPath("$.motivoRecusa")
                .value(
                    "Pagamento recusado pelo gateway mock"
                ))
            .andExpect(jsonPath("$.transacaoId")
                .isNotEmpty())
            .andExpect(jsonPath("$.processadoEm")
                .isNotEmpty());

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("PAGAMENTO_RECUSADO");

        assertThat(pontosDoCliente())
            .isEqualTo(50);

        assertNenhumCreditoRegistrado(pedidoId);

        assertPagamentoRecusado(
            pedidoId,
            new BigDecimal("58.30")
        );

        /*
         * Os produtos devem retornar integralmente
         * ao estoque da unidade.
         */
        assertThat(estoqueDoProduto(101L))
            .isEqualTo(50);

        assertThat(estoqueDoProduto(104L))
            .isEqualTo(50);
    }

    @Test
    void deveCriarCarteiraQuandoClienteAindaNaoPossuiUma()
        throws Exception {

        jdbcTemplate.update(
            """
                DELETE FROM carteiras_fidelidade
                WHERE cliente_id = ?::uuid
                """,
            CLIENTE_ID
        );

        assertThat(quantidadeCarteirasDoCliente())
            .isZero();

        UUID pedidoId = criarPedidoPadrao();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status")
                .value("PAGO"))
            .andExpect(jsonPath("$.statusPagamento")
                .value("APROVADO"))
            .andExpect(jsonPath("$.valorTotal")
                .value(58.3));

        assertThat(quantidadeCarteirasDoCliente())
            .isEqualTo(1L);

        assertThat(pontosDoCliente())
            .isEqualTo(5);

        assertCreditoRegistrado(
            pedidoId,
            5
        );

        assertPagamentoAprovado(
            pedidoId,
            new BigDecimal("58.30")
        );
    }

    @Test
    void naoDeveCreditarPontosSemConsentimento()
        throws Exception {

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

        UUID pedidoId = criarPedidoPadrao();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status")
                .value("PAGO"))
            .andExpect(jsonPath("$.statusPagamento")
                .value("APROVADO"));

        assertThat(pontosDoCliente())
            .isEqualTo(50);

        assertNenhumCreditoRegistrado(pedidoId);
        assertPagamentoAprovado(
            pedidoId,
            new BigDecimal("58.30")
        );
    }

    @Test
    void deveRetornar404ParaPedidoInexistente()
        throws Exception {

        UUID pedidoId = UUID.randomUUID();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("PEDIDO_NAO_ENCONTRADO"));

        assertNenhumCreditoRegistrado(pedidoId);
        assertNenhumPagamentoRegistrado(pedidoId);
    }

    @Test
    void deveRecusarCupomInexistenteERestaurarTransacao()
        throws Exception {

        UUID pedidoId = criarPedidoPadrao();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
                    .param(
                        "codigoPromocional",
                        "CUPOM_INEXISTENTE"
                    )
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("CUPOM_INVALIDO"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("AGUARDANDO_PAGAMENTO");

        assertThat(pontosDoCliente())
            .isEqualTo(50);

        assertNenhumCreditoRegistrado(pedidoId);
        assertNenhumPagamentoRegistrado(pedidoId);

        /*
         * Como o checkout não foi processado, o pedido
         * continua reservando seus produtos.
         */
        assertThat(estoqueDoProduto(101L))
            .isEqualTo(48);

        assertThat(estoqueDoProduto(104L))
            .isEqualTo(49);
    }

    @Test
    void deveAplicarCupomIgnorandoMaiusculasEMinusculas()
        throws Exception {

        UUID pedidoId = criarPedidoPadrao();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
                    .param(
                        "codigoPromocional",
                        "bemvindo10"
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status")
                .value("PAGO"))
            .andExpect(jsonPath("$.valorTotal")
                .value(52.47))
            .andExpect(jsonPath("$.statusPagamento")
                .value("APROVADO"));

        assertThat(pontosDoCliente())
            .isEqualTo(55);

        assertCreditoRegistrado(
            pedidoId,
            5
        );

        assertPagamentoAprovado(
            pedidoId,
            new BigDecimal("52.47")
        );
    }

    @Test
    void deveRecusarCupomAbaixoDoValorMinimo()
        throws Exception {

        String itens = """
            [
              {
                "produtoId": 104,
                "quantidade": 1
              }
            ]
            """;

        UUID pedidoId = criarPedido(itens);

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
                    .param(
                        "codigoPromocional",
                        "BEMVINDO10"
                    )
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("VALOR_MINIMO_NAO_ATINGIDO"));

        assertThat(statusDoPedido(pedidoId))
            .isEqualTo("AGUARDANDO_PAGAMENTO");

        assertThat(pontosDoCliente())
            .isEqualTo(50);

        assertNenhumCreditoRegistrado(pedidoId);
        assertNenhumPagamentoRegistrado(pedidoId);

        assertThat(estoqueDoProduto(104L))
            .isEqualTo(49);
    }

    private UUID criarPedidoPadrao() throws Exception {
        String itens = """
            [
              {
                "produtoId": 101,
                "quantidade": 2
              },
              {
                "produtoId": 104,
                "quantidade": 1
              }
            ]
            """;

        return criarPedido(itens);
    }

    private UUID criarPedido(String itens)
        throws Exception {

        String request = """
            {
              "clienteId": "%s",
              "unidadeId": "%s",
              "canalPedido": "APP",
              "itens": %s
            }
            """.formatted(
                CLIENTE_ID,
                UNIDADE_RECIFE,
                itens
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

    private void assertCreditoRegistrado(
        UUID pedidoId,
        int pontosEsperados
    ) {
        assertThat(quantidadeHistoricosDoPedido(pedidoId))
            .isEqualTo(1L);

        assertThat(pontosRegistradosNoHistorico(pedidoId))
            .isEqualTo((long) pontosEsperados);

        assertThat(tipoOperacaoDoHistorico(pedidoId))
            .isEqualTo("CREDITO");
    }

    private void assertNenhumCreditoRegistrado(
        UUID pedidoId
    ) {
        assertThat(quantidadeHistoricosDoPedido(pedidoId))
            .isZero();

        assertThat(pontosRegistradosNoHistorico(pedidoId))
            .isZero();
    }

    private void assertPagamentoAprovado(
        UUID pedidoId,
        BigDecimal valorEsperado
    ) {
        assertThat(quantidadePagamentosDoPedido(pedidoId))
            .isEqualTo(1L);

        assertThat(statusPagamentoDoPedido(pedidoId))
            .isEqualTo("APROVADO");

        assertThat(valorPagamentoDoPedido(pedidoId))
            .isEqualByComparingTo(valorEsperado);

        assertThat(gatewayPagamentoDoPedido(pedidoId))
            .isEqualTo("MOCK_GATEWAY");

        assertThat(
            quantidadePagamentosSemMotivoRecusa(pedidoId)
        ).isEqualTo(1L);
    }

    private void assertPagamentoRecusado(
        UUID pedidoId,
        BigDecimal valorEsperado
    ) {
        assertThat(quantidadePagamentosDoPedido(pedidoId))
            .isEqualTo(1L);

        assertThat(statusPagamentoDoPedido(pedidoId))
            .isEqualTo("RECUSADO");

        assertThat(valorPagamentoDoPedido(pedidoId))
            .isEqualByComparingTo(valorEsperado);

        assertThat(gatewayPagamentoDoPedido(pedidoId))
            .isEqualTo("MOCK_GATEWAY");

        assertThat(motivoRecusaDoPagamento(pedidoId))
            .isEqualTo(
                "Pagamento recusado pelo gateway mock"
            );
    }

    private void assertNenhumPagamentoRegistrado(
        UUID pedidoId
    ) {
        assertThat(quantidadePagamentosDoPedido(pedidoId))
            .isZero();
    }

    private Integer pontosDoCliente() {
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

    private Integer estoqueDoProduto(Long produtoId) {
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

    private Long quantidadeCarteirasDoCliente() {
        return jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM carteiras_fidelidade
                WHERE cliente_id = ?::uuid
                """,
            Long.class,
            CLIENTE_ID
        );
    }

    private Long quantidadeHistoricosDoPedido(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM historico_pontos
                WHERE pedido_id = ?
                """,
            Long.class,
            pedidoId
        );
    }

    private Long pontosRegistradosNoHistorico(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT COALESCE(
                    SUM(pontos_alterados),
                    0
                )
                FROM historico_pontos
                WHERE pedido_id = ?
                """,
            Long.class,
            pedidoId
        );
    }

    private String tipoOperacaoDoHistorico(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT tipo_operacao
                FROM historico_pontos
                WHERE pedido_id = ?
                """,
            String.class,
            pedidoId
        );
    }

    private Long quantidadePagamentosDoPedido(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM tb_pagamentos
                WHERE pedido_id = ?
                """,
            Long.class,
            pedidoId
        );
    }

    private String statusPagamentoDoPedido(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT status
                FROM tb_pagamentos
                WHERE pedido_id = ?
                """,
            String.class,
            pedidoId
        );
    }

    private BigDecimal valorPagamentoDoPedido(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT valor
                FROM tb_pagamentos
                WHERE pedido_id = ?
                """,
            BigDecimal.class,
            pedidoId
        );
    }

    private String gatewayPagamentoDoPedido(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT gateway
                FROM tb_pagamentos
                WHERE pedido_id = ?
                """,
            String.class,
            pedidoId
        );
    }

    private String motivoRecusaDoPagamento(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT motivo_recusa
                FROM tb_pagamentos
                WHERE pedido_id = ?
                """,
            String.class,
            pedidoId
        );
    }

    private Long quantidadePagamentosSemMotivoRecusa(
        UUID pedidoId
    ) {
        return jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM tb_pagamentos
                WHERE pedido_id = ?
                  AND motivo_recusa IS NULL
                """,
            Long.class,
            pedidoId
        );
    }

    private String statusDoPedido(UUID pedidoId) {
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

        /*
         * O pagamento deve ser removido antes do pedido
         * devido à chave estrangeira.
         */
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
