package com.raizes.nordeste.pedidos.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesGcmEncryptionServiceTest {

    private static final String TEST_KEY =
        "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3"
            + "ODlhYmNkZWY=";

    private static final String PREFIX = "ENC:v1:";

    private AesGcmEncryptionService encryptionService;

    @BeforeEach
    void prepararServico() {
        encryptionService =
            new AesGcmEncryptionService(TEST_KEY);
    }

    @Test
    void deveCriptografarEDescriptografar()
        throws Exception {

        String plaintext =
            "dado sensível com acentuação";

        String encrypted =
            encryptionService.encrypt(plaintext);

        assertThat(encrypted)
            .startsWith(PREFIX)
            .isNotEqualTo(plaintext);

        assertThat(
            encryptionService.decrypt(encrypted)
        ).isEqualTo(plaintext);
    }

    @Test
    void deveGerarResultadosDiferentesParaMesmoValor()
        throws Exception {

        String plaintext = "12345678901";

        String first =
            encryptionService.encrypt(plaintext);

        String second =
            encryptionService.encrypt(plaintext);

        assertThat(first).isNotEqualTo(second);

        assertThat(
            encryptionService.decrypt(first)
        ).isEqualTo(plaintext);

        assertThat(
            encryptionService.decrypt(second)
        ).isEqualTo(plaintext);
    }

    @Test
    void deveDetectarConteudoAdulterado()
        throws Exception {

        String encrypted =
            encryptionService.encrypt(
                "teste@email.com"
            );

        byte[] payload = Base64
            .getDecoder()
            .decode(
                encrypted.substring(
                    PREFIX.length()
                )
            );

        payload[payload.length - 1] ^= 1;

        String tampered = PREFIX
            + Base64.getEncoder()
                .encodeToString(payload);

        assertThatThrownBy(() ->
            encryptionService.decrypt(tampered)
        )
            .isInstanceOf(
                IllegalStateException.class
            )
            .hasMessageContaining("adulterado");
    }

    @Test
    void deveRejeitarChaveQueNaoSejaBase64() {
        assertThatThrownBy(() ->
            new AesGcmEncryptionService(
                "chave-que-nao-e-base64"
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
            new AesGcmEncryptionService(shortKey)
        )
            .isInstanceOf(
                IllegalStateException.class
            )
            .hasMessageContaining("32 bytes");
    }

    @Test
    void deveTratarValoresNulos() {
        assertThat(
            encryptionService.encrypt(null)
        ).isNull();

        assertThat(
            encryptionService.decrypt(null)
        ).isNull();
    }

    @Test
    void deveRejeitarConteudoSemPrefixo() {
        assertThatThrownBy(() ->
            encryptionService.decrypt(
                "conteudo-sem-prefixo"
            )
        )
            .isInstanceOf(
                IllegalArgumentException.class
            )
            .hasMessageContaining(
                "formato de criptografia"
            );
    }
}