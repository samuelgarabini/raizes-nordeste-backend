package com.raizes.nordeste.pedidos.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CardapioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarCardapioDaUnidadeRecife() throws Exception {
        mockMvc.perform(get(
                "/api/v1/unidades/{unidadeId}/cardapio",
                "550e8400-e29b-41d4-a716-446655440000"
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unidadeId")
                .value("550e8400-e29b-41d4-a716-446655440000"))
            .andExpect(jsonPath("$.itens", hasSize(4)))
            .andExpect(jsonPath("$.itens[*].nome", hasItem("Baião de Dois")))
            .andExpect(jsonPath("$.itens[*].nome", hasItem("Cuscuz com Carne de Sol")))
            .andExpect(jsonPath(
                "$.itens[*].nome",
                not(hasItem("Produto Indisponível"))
            ));
    }

    @Test
    void deveRetornarCardapioDaUnidadeSalvador() throws Exception {
        mockMvc.perform(get(
                "/api/v1/unidades/{unidadeId}/cardapio",
                "a1b2c3d4-e5f6-7890-abcd-ef1234567891"
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unidadeId")
                .value("a1b2c3d4-e5f6-7890-abcd-ef1234567891"))
            .andExpect(jsonPath("$.itens", hasSize(4)))
            .andExpect(jsonPath("$.itens[*].nome", hasItem("Acarajé")))
            .andExpect(jsonPath("$.itens[*].nome", hasItem("Moqueca Baiana")));
    }
}