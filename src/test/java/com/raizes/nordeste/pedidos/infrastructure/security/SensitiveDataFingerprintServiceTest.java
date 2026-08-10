package com.raizes.nordeste.pedidos.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensitiveDataFingerprintServiceTest {

    private static final String TEST_KEY =
        "ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4"
            + "NzY1NDMyMTA=";

    private SensitiveDataFingerprintService
        fingerprintService;

    @BeforeEach
    void prepararServico() {
        fingerprintService =
            new SensitiveDataFingerprintService(
                TEST_KEY
            );
    }

    @Test
    void deveGerarImpressaoDeterministica() {
        String first =
            fingerprintService.fingerprintCpf(
                "12345678901"
            );

        String second =
            fingerprintService.fingerprintCpf(
                "12345678901"
            );

        assertThat(first)
            .isEqualTo(second)
            .hasSize(64);
    }

    @Test
    void deveIgnorarFormatacaoDoCpf() {
        String unformatted =
            fingerprintService.fingerprintCpf(
                "12345678901"
            );

        String formatted =
            fingerprintService.fingerprintCpf(
                "123.456.789-01"
            );

        assertThat(formatted)
            .isEqualTo(unformatted);
    }

    @Test
    void deveGerarImpressoesDiferentes() {
        String first =
            fingerprintService.fingerprintCpf(
                "12345678901"
            );

        String second =
            fingerprintService.fingerprintCpf(
                "98765432100"
            );

        assertThat(first)
            .isNotEqualTo(second);
    }

    @Test
    void naoDeveExporCpfNaImpressao() {
        String fingerprint =
            fingerprintService.fingerprintCpf(
                "12345678901"
            );

        assertThat(fingerprint)
            .doesNotContain("12345678901")
            .matches("[0-9a-f]{64}");
    }

    @Test
    void deveTratarCpfNulo() {
        assertThat(
            fingerprintService.fingerprintCpf(null)
        ).isNull();
    }

    @Test
    void deveGerarImpressaoGenericaDeterministica() {
        String first =
            fingerprintService.fingerprint(
                "AUDIT_ACTOR",
                "admin"
            );

        String second =
            fingerprintService.fingerprint(
                "audit_actor",
                " ADMIN "
            );

        assertThat(first)
            .isEqualTo(second)
            .hasSize(64)
            .matches("[0-9a-f]{64}");
    }

    @Test
    void deveSepararContextosDaImpressao() {
        String actorFingerprint =
            fingerprintService.fingerprint(
                "AUDIT_ACTOR",
                "127.0.0.1"
            );

        String ipFingerprint =
            fingerprintService.fingerprint(
                "AUDIT_IP",
                "127.0.0.1"
            );

        assertThat(actorFingerprint)
            .isNotEqualTo(ipFingerprint);

        assertThat(actorFingerprint)
            .hasSize(64);

        assertThat(ipFingerprint)
            .hasSize(64);
    }

    @Test
    void deveRejeitarChaveForaDeBase64() {
        assertThatThrownBy(() ->
            new SensitiveDataFingerprintService(
                "nao-e-base64"
            )
        )
            .isInstanceOf(
                IllegalStateException.class
            )
            .hasMessageContaining("Base64");
    }

    @Test
    void deveRejeitarChaveComTamanhoIncorreto() {
        String shortKey = Base64
            .getEncoder()
            .encodeToString(new byte[16]);

        assertThatThrownBy(() ->
            new SensitiveDataFingerprintService(
                shortKey
            )
        )
            .isInstanceOf(
                IllegalStateException.class
            )
            .hasMessageContaining("32 bytes");
    }
}