package com.raizes.nordeste.pedidos.infrastructure.security;

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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AutenticacaoAuditoriaIntegrationTest {

    private static final String CORRECT_PASSWORD =
        "Senha@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {
        limparAuditoria();
        ativarUsuarios();
    }

    @AfterEach
    void restaurarBanco() {
        limparAuditoria();
        ativarUsuarios();
    }

    @Test
    void deveAuditarLoginComSucesso()
        throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content(
                        criarLoginJson(
                            "admin",
                            CORRECT_PASSWORD
                        )
                    )
                    .with(request ->
                        {
                            request.setRemoteAddr(
                                "127.0.0.1"
                            );
                            return request;
                        }
                    )
            )
            .andExpect(status().isOk());

        Map<String, Object> audit =
            buscarUltimaAuditoria();

        assertThat(audit.get("evento"))
            .isEqualTo("LOGIN");

        assertThat(audit.get("resultado"))
            .isEqualTo("SUCESSO");

        assertThat(audit.get("perfil"))
            .isEqualTo("ADMIN");

        assertThat(audit.get("recurso"))
            .isEqualTo("AUTENTICACAO");

        assertThat(audit.get("codigo_erro"))
            .isNull();

        assertThat(
            audit.get("ator_fingerprint")
        )
            .asString()
            .hasSize(64)
            .doesNotContain("admin");

        assertThat(
            audit.get("ip_fingerprint")
        )
            .asString()
            .hasSize(64)
            .doesNotContain("127.0.0.1");
    }

    @Test
    void deveAuditarFalhaDeLogin()
        throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content(
                        criarLoginJson(
                            "usuario-inexistente",
                            "senha-incorreta"
                        )
                    )
                    .with(request ->
                        {
                            request.setRemoteAddr(
                                "192.168.1.10"
                            );
                            return request;
                        }
                    )
            )
            .andExpect(status().isUnauthorized());

        Map<String, Object> audit =
            buscarUltimaAuditoria();

        assertThat(audit.get("evento"))
            .isEqualTo("LOGIN");

        assertThat(audit.get("resultado"))
            .isEqualTo("FALHA");

        assertThat(audit.get("perfil"))
            .isNull();

        assertThat(audit.get("codigo_erro"))
            .isEqualTo("CREDENCIAIS_INVALIDAS");

        assertThat(
            audit.get("ator_fingerprint")
        )
            .asString()
            .doesNotContain(
                "usuario-inexistente"
            );

        assertThat(
            audit.get("ip_fingerprint")
        )
            .asString()
            .doesNotContain("192.168.1.10");
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

    private Map<String, Object>
        buscarUltimaAuditoria() {

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
                ORDER BY ocorrido_em DESC
                LIMIT 1
                """
        );
    }

    private void limparAuditoria() {
        jdbcTemplate.update(
            "DELETE FROM tb_auditoria_seguranca"
        );
    }

    private void ativarUsuarios() {
        jdbcTemplate.update(
            """
                UPDATE tb_usuarios
                SET ativo = TRUE,
                    atualizado_em =
                        CURRENT_TIMESTAMP
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