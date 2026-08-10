package com.raizes.nordeste.pedidos.infrastructure.audit;

import com.raizes.nordeste.pedidos.infrastructure.security.Perfil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditoriaSegurancaIntegrationTest {

    @Autowired
    private AuditoriaSegurancaService
        auditoriaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararBanco() {
        limparAuditoria();
    }

    @AfterEach
    void restaurarBanco() {
        limparAuditoria();
    }

    @Test
    void deveRegistrarAuditoriaSemDadosCrus() {
        auditoriaService.registrar(
            new RegistrarAuditoriaCommand(
                TipoEventoAuditoria.LOGIN,
                ResultadoAuditoria.SUCESSO,
                "admin",
                Perfil.ADMIN,
                "AUTENTICACAO",
                null,
                "127.0.0.1",
                null
            )
        );

        Map<String, Object> registro =
            buscarUltimoRegistro();

        assertThat(registro.get("evento"))
            .isEqualTo("LOGIN");

        assertThat(registro.get("resultado"))
            .isEqualTo("SUCESSO");

        assertThat(registro.get("perfil"))
            .isEqualTo("ADMIN");

        assertThat(
            registro.get("ator_fingerprint")
        )
            .asString()
            .hasSize(64)
            .doesNotContain("admin");

        assertThat(
            registro.get("ip_fingerprint")
        )
            .asString()
            .hasSize(64)
            .doesNotContain("127.0.0.1");

        assertThat(registro.get("ocorrido_em"))
            .isNotNull();
    }

    @Test
    void deveRegistrarFalhaSemRevelarUsuario() {
        auditoriaService.registrar(
            new RegistrarAuditoriaCommand(
                TipoEventoAuditoria.LOGIN,
                ResultadoAuditoria.FALHA,
                "usuario-inexistente",
                null,
                "AUTENTICACAO",
                null,
                "192.168.1.10",
                "CREDENCIAIS_INVALIDAS"
            )
        );

        Map<String, Object> registro =
            buscarUltimoRegistro();

        assertThat(registro.get("resultado"))
            .isEqualTo("FALHA");

        assertThat(registro.get("perfil"))
            .isNull();

        assertThat(registro.get("codigo_erro"))
            .isEqualTo("CREDENCIAIS_INVALIDAS");

        assertThat(
            registro.get("ator_fingerprint")
        )
            .asString()
            .doesNotContain(
                "usuario-inexistente"
            );

        assertThat(
            registro.get("ip_fingerprint")
        )
            .asString()
            .doesNotContain("192.168.1.10");
    }

    private Map<String, Object>
        buscarUltimoRegistro() {

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
                    codigo_erro,
                    ocorrido_em
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
}