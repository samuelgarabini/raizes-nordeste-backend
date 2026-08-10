package com.raizes.nordeste.pedidos.infrastructure.security;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AutenticacaoIntegrationTest {

    private static final String SENHA_CORRETA =
        "Senha@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararUsuarios() {
        ativarUsuarios();
    }

    @AfterEach
    void restaurarUsuarios() {
        ativarUsuarios();
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas()
        throws Exception {

        String token = autenticar(
            "ADMIN",
            SENHA_CORRETA
        );

        assertThat(token).isNotBlank();
    }

    @Test
    void deveRetornar401ParaSenhaIncorreta()
        throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        criarLoginJson(
                            "admin",
                            "senha-incorreta"
                        )
                    )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void deveRetornar401ParaUsuarioInexistente()
        throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        criarLoginJson(
                            "usuario-inexistente",
                            SENHA_CORRETA
                        )
                    )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void deveRetornar401ParaUsuarioInativo()
        throws Exception {

        desativarUsuario("atendente");

        mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        criarLoginJson(
                            "atendente",
                            SENHA_CORRETA
                        )
                    )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void deveRetornar400ParaCamposVazios()
        throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        criarLoginJson("", "")
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("VALIDATION_ERROR"));
    }

    @Test
    void deveRetornar401SemToken()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos/health")
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("NAO_AUTENTICADO"))
            .andExpect(jsonPath("$.path")
                .value("/api/v1/pedidos/health"));
    }

    @Test
    void deveRetornar401ParaTokenInvalido()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/pedidos/health")
                    .header(
                        "Authorization",
                        "Bearer token-invalido"
                    )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("NAO_AUTENTICADO"));
    }

    @Test
    void deveAutorizarTokenValido()
        throws Exception {

        String token = autenticar(
            "admin",
            SENHA_CORRETA
        );

        mockMvc.perform(
                get("/api/v1/pedidos/health")
                    .header(
                        "Authorization",
                        "Bearer " + token
                    )
            )
            .andExpect(status().isOk());
    }

    @Test
    void deveInvalidarTokenQuandoUsuarioForDesativado()
        throws Exception {

        String token = autenticar(
            "admin",
            SENHA_CORRETA
        );

        desativarUsuario("admin");

        mockMvc.perform(
                get("/api/v1/pedidos/health")
                    .header(
                        "Authorization",
                        "Bearer " + token
                    )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("NAO_AUTENTICADO"));
    }

    @Test
    void deveRetornar403ParaPerfilSemPermissao()
        throws Exception {

        String token = autenticar(
            "cliente",
            SENHA_CORRETA
        );

        mockMvc.perform(
                patch(
                    "/api/v1/pedidos/{id}/status",
                    UUID.randomUUID()
                )
                    .param(
                        "novoStatus",
                        "EM_PREPARACAO"
                    )
                    .header(
                        "Authorization",
                        "Bearer " + token
                    )
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error")
                .value("ACESSO_NEGADO"));
    }

    private String autenticar(
        String username,
        String password
    ) throws Exception {
        MvcResult result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        criarLoginJson(
                            username,
                            password
                        )
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo")
                .value("Bearer"))
            .andExpect(jsonPath("$.expiracaoEm")
                .isNumber())
            .andReturn();

        JsonNode response = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        return response.get("token").asText();
    }

    private String criarLoginJson(
        String username,
        String password
    ) throws Exception {
        return objectMapper.writeValueAsString(
            Map.of(
                "username",
                username,
                "password",
                password
            )
        );
    }

    private void desativarUsuario(String username) {
        jdbcTemplate.update(
            """
                UPDATE tb_usuarios
                SET ativo = FALSE,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE LOWER(username) = LOWER(?)
                """,
            username
        );
    }

    private void ativarUsuarios() {
        jdbcTemplate.update(
            """
                UPDATE tb_usuarios
                SET ativo = TRUE,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE username IN (
                    'admin',
                    'gerente',
                    'atendente',
                    'cliente'
                )
                """
        );
    }
}