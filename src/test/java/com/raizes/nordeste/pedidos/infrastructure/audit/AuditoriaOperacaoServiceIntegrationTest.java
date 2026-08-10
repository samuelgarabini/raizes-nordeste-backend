package com.raizes.nordeste.pedidos.infrastructure.audit;

import com.raizes.nordeste.pedidos.infrastructure.exception.BusinessConflictException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditoriaOperacaoServiceIntegrationTest {

    @Autowired
    private AuditoriaOperacaoService
        auditoriaOperacaoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararContexto() {
        limparAuditoria();

        UsernamePasswordAuthenticationToken
            authentication =
                new UsernamePasswordAuthenticationToken(
                    "atendente",
                    null,
                    List.of(
                        new SimpleGrantedAuthority(
                            "ROLE_ATENDENTE"
                        )
                    )
                );

        SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);

        MockHttpServletRequest request =
            new MockHttpServletRequest();

        request.setRemoteAddr("10.0.0.25");

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request)
        );
    }

    @AfterEach
    void restaurarContexto() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
        limparAuditoria();
    }

    @Test
    void deveRegistrarSucessoComContextoSeguro() {
        UUID pedidoId = UUID.randomUUID();

        auditoriaOperacaoService.registrarSucesso(
            TipoEventoAuditoria
                .ALTERACAO_STATUS_PEDIDO,
            "PEDIDO",
            pedidoId
        );

        Map<String, Object> audit =
            buscarUltimaAuditoria();

        assertThat(audit.get("evento"))
            .isEqualTo(
                "ALTERACAO_STATUS_PEDIDO"
            );

        assertThat(audit.get("resultado"))
            .isEqualTo("SUCESSO");

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
            .hasSize(64)
            .doesNotContain("10.0.0.25");
    }

    @Test
    void deveRegistrarCodigoDaFalha() {
        UUID pedidoId = UUID.randomUUID();

        BusinessConflictException exception =
            new BusinessConflictException(
                "TRANSICAO_STATUS_INVALIDA",
                "Transição inválida para o teste"
            );

        auditoriaOperacaoService.registrarFalha(
            TipoEventoAuditoria
                .ALTERACAO_STATUS_PEDIDO,
            "PEDIDO",
            pedidoId,
            exception
        );

        Map<String, Object> audit =
            buscarUltimaAuditoria();

        assertThat(audit.get("resultado"))
            .isEqualTo("FALHA");

        assertThat(audit.get("codigo_erro"))
            .isEqualTo(
                "TRANSICAO_STATUS_INVALIDA"
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
}