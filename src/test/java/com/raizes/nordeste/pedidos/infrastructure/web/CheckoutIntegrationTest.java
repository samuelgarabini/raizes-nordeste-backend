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
    void deveProcessarCheckoutApenasUmaVez() throws Exception {
        UUID pedidoId = criarPedidoPadrao();

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAGO"))
            .andExpect(jsonPath("$.valorTotal").value(58.3));

        assertThat(pontosDoCliente()).isEqualTo(55);

        mockMvc.perform(
                post(
                    "/api/v1/pedidos/{id}/checkout",
                    pedidoId
                )
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("CHECKOUT_NAO_PERMITIDO"));

        assertThat(pontosDoCliente()).isEqualTo(55);
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

        assertThat(pontosDoCliente()).isEqualTo(50);
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
            .andExpect(jsonPath("$.status").value("PAGO"))
            .andExpect(jsonPath("$.valorTotal").value(52.47));

        assertThat(pontosDoCliente()).isEqualTo(55);
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

        assertThat(pontosDoCliente()).isEqualTo(50);
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

    private UUID criarPedido(String itens) throws Exception {
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

        jdbcTemplate.update(
            "DELETE FROM tb_pedido_itens"
        );

        jdbcTemplate.update(
            "DELETE FROM tb_outbox"
        );

        jdbcTemplate.update(
            "DELETE FROM tb_pedidos"
        );

        jdbcTemplate.update("""
            UPDATE carteiras_fidelidade
            SET pontos_acumulados = 50,
                ultima_atualizacao = CURRENT_TIMESTAMP
            WHERE cliente_id =
                'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
            """);

        jdbcTemplate.update("""
            UPDATE campanhas
            SET ativo = TRUE,
                data_inicio =
                    CURRENT_TIMESTAMP - INTERVAL '1 day',
                data_fim =
                    CURRENT_TIMESTAMP + INTERVAL '30 days'
            WHERE codigo_promocional = 'BEMVINDO10'
            """);

        jdbcTemplate.update("""
            UPDATE tb_estoques estoque
            SET quantidade = CASE
                    WHEN produto.disponivel = TRUE THEN 50
                    ELSE 0
                END,
                atualizado_em = CURRENT_TIMESTAMP
            FROM tb_produtos produto
            WHERE produto.id = estoque.produto_id
            """);
    }
}