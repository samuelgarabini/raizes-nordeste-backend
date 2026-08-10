package com.raizes.nordeste.pedidos.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raizes.nordeste.pedidos.repository.EstoqueRepository;
import com.raizes.nordeste.pedidos.repository.ItemPedidoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoIntegrationTest {

    private static final String CLIENTE_ID =
        "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";

    private static final String UNIDADE_RECIFE =
        "550e8400-e29b-41d4-a716-446655440000";

    private static final String UNIDADE_SALVADOR =
        "a1b2c3d4-e5f6-7890-abcd-ef1234567891";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {
        limparPedidosERestaurarEstoque();
    }

    @AfterEach
    void limparBanco() {
        limparPedidosERestaurarEstoque();
    }

    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    void deveCriarPedidoCalcularTotalEBaixarEstoque() throws Exception {
        String request = """
            {
              "clienteId": "%s",
              "unidadeId": "%s",
              "canalPedido": "TOTEM",
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
            """.formatted(CLIENTE_ID, UNIDADE_RECIFE);

        MvcResult result = mockMvc.perform(
                post("/api/v1/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.clienteId").value(CLIENTE_ID))
            .andExpect(jsonPath("$.unidadeId").value(UNIDADE_RECIFE))
            .andExpect(jsonPath("$.canalPedido").value("TOTEM"))
            .andExpect(jsonPath("$.valorTotal").value(58.3))
            .andExpect(jsonPath("$.status")
                .value("AGUARDANDO_PAGAMENTO"))
            .andExpect(jsonPath("$.itens.length()").value(2))
            .andExpect(jsonPath("$.itens[0].produtoId").value(101))
            .andExpect(jsonPath("$.itens[0].subtotal").value(49.8))
            .andExpect(jsonPath("$.itens[1].produtoId").value(104))
            .andExpect(jsonPath("$.itens[1].subtotal").value(8.5))
            .andReturn();

        JsonNode response = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        UUID pedidoId = UUID.fromString(
            response.get("id").asText()
        );

        assertThat(
            itemPedidoRepository.findByPedidoId(pedidoId)
        ).hasSize(2);

        assertThat(quantidadeEmEstoque(UNIDADE_RECIFE, 101L))
            .isEqualTo(48);

        assertThat(quantidadeEmEstoque(UNIDADE_RECIFE, 104L))
            .isEqualTo(49);
    }

    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    void deveRetornar404ParaProdutoDeOutraUnidade() throws Exception {
        String request = """
            {
              "clienteId": "%s",
              "unidadeId": "%s",
              "canalPedido": "APP",
              "itens": [
                {
                  "produtoId": 201,
                  "quantidade": 1
                }
              ]
            }
            """.formatted(CLIENTE_ID, UNIDADE_RECIFE);

        mockMvc.perform(
                post("/api/v1/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("PRODUTO_NAO_ENCONTRADO"));

        assertThat(quantidadeEmEstoque(UNIDADE_SALVADOR, 201L))
            .isEqualTo(50);
    }

    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    void deveRetornar409QuandoEstoqueForInsuficiente() throws Exception {
        String request = """
            {
              "clienteId": "%s",
              "unidadeId": "%s",
              "canalPedido": "WEB",
              "itens": [
                {
                  "produtoId": 101,
                  "quantidade": 51
                }
              ]
            }
            """.formatted(CLIENTE_ID, UNIDADE_RECIFE);

        mockMvc.perform(
                post("/api/v1/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("ESTOQUE_INSUFICIENTE"));

        assertThat(quantidadeEmEstoque(UNIDADE_RECIFE, 101L))
            .isEqualTo(50);
    }

    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    void deveRetornar400QuandoPedidoNaoTiverItens() throws Exception {
        String request = """
            {
              "clienteId": "%s",
              "unidadeId": "%s",
              "canalPedido": "BALCAO",
              "itens": []
            }
            """.formatted(CLIENTE_ID, UNIDADE_RECIFE);

        mockMvc.perform(
                post("/api/v1/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details[0].field")
                .value("itens"));
    }

    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    void deveFazerRollbackQuandoProdutoForDuplicado() throws Exception {
        String request = """
            {
              "clienteId": "%s",
              "unidadeId": "%s",
              "canalPedido": "PICKUP",
              "itens": [
                {
                  "produtoId": 101,
                  "quantidade": 1
                },
                {
                  "produtoId": 101,
                  "quantidade": 1
                }
              ]
            }
            """.formatted(CLIENTE_ID, UNIDADE_RECIFE);

        mockMvc.perform(
                post("/api/v1/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error")
                .value("PRODUTO_DUPLICADO"));

        assertThat(quantidadeEmEstoque(UNIDADE_RECIFE, 101L))
            .isEqualTo(50);
    }

    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    void deveRetornar404ParaUnidadeInexistente() throws Exception {
        String unidadeInexistente =
            "11111111-1111-1111-1111-111111111111";

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
            """.formatted(CLIENTE_ID, unidadeInexistente);

        mockMvc.perform(
                post("/api/v1/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("UNIDADE_NAO_ENCONTRADA"));

        assertThat(quantidadeEmEstoque(UNIDADE_RECIFE, 101L))
            .isEqualTo(50);
    }

    private int quantidadeEmEstoque(
        String unidadeId,
        Long produtoId
    ) {
        return estoqueRepository
            .findByUnidadeIdAndProdutoId(
                UUID.fromString(unidadeId),
                produtoId
            )
            .orElseThrow()
            .getQuantidade();
    }

    private void limparPedidosERestaurarEstoque() {
        jdbcTemplate.update("DELETE FROM tb_pedido_itens");
        jdbcTemplate.update("DELETE FROM tb_outbox");
        jdbcTemplate.update("DELETE FROM tb_pedidos");

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