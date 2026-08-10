package com.raizes.nordeste.pedidos.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class SensitiveDataFingerprintService {

    private static final String HMAC_ALGORITHM =
        "HmacSHA256";

    private static final int KEY_LENGTH_BYTES = 32;

    private final SecretKeySpec secretKey;

    public SensitiveDataFingerprintService(
        @Value(
            "${api.security.encryption.fingerprint-key}"
        )
        String encodedKey
    ) {
        this.secretKey = criarChave(encodedKey);
    }

    public String fingerprintCpf(String cpf) {
        if (cpf == null) {
            return null;
        }

        return gerarFingerprint(
            normalizarCpf(cpf)
        );
    }

    public String fingerprint(
        String contexto,
        String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        if (
            contexto == null
                || contexto.isBlank()
        ) {
            throw new IllegalArgumentException(
                "O contexto da impressão "
                    + "é obrigatório"
            );
        }

        String normalizedContext = contexto
            .trim()
            .toUpperCase(Locale.ROOT);

        String normalizedValue = value
            .trim()
            .toUpperCase(Locale.ROOT);

        return gerarFingerprint(
            normalizedContext
                + ":"
                + normalizedValue
        );
    }

    private String gerarFingerprint(
        String normalizedValue
    ) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);

            byte[] fingerprint = mac.doFinal(
                normalizedValue.getBytes(
                    StandardCharsets.UTF_8
                )
            );

            return HexFormat
                .of()
                .formatHex(fingerprint);
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Não foi possível gerar "
                    + "a impressão HMAC",
                exception
            );
        }
    }

    private String normalizarCpf(String cpf) {
        String trimmedCpf = cpf.trim();

        if (trimmedCpf.isBlank()) {
            throw new IllegalArgumentException(
                "O CPF não pode estar vazio"
            );
        }

        String digits = trimmedCpf.replaceAll(
            "\\D",
            ""
        );

        if (digits.length() == 11) {
            return digits;
        }

        return trimmedCpf.toUpperCase(
            Locale.ROOT
        );
    }

    private SecretKeySpec criarChave(
        String encodedKey
    ) {
        if (
            encodedKey == null
                || encodedKey.isBlank()
        ) {
            throw new IllegalStateException(
                "DATA_FINGERPRINT_KEY "
                    + "não foi configurada"
            );
        }

        final byte[] keyBytes;

        try {
            keyBytes = Base64
                .getDecoder()
                .decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                "DATA_FINGERPRINT_KEY deve estar "
                    + "codificada em Base64",
                exception
            );
        }

        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                "DATA_FINGERPRINT_KEY deve representar "
                    + "exatamente 32 bytes"
            );
        }

        return new SecretKeySpec(
            keyBytes,
            HMAC_ALGORITHM
        );
    }
}