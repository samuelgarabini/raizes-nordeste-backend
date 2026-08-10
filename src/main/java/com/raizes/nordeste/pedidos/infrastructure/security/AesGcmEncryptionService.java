package com.raizes.nordeste.pedidos.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class AesGcmEncryptionService {

    private static final String TRANSFORMATION =
        "AES/GCM/NoPadding";

    private static final String KEY_ALGORITHM = "AES";

    private static final String PREFIX = "ENC:v1:";

    private static final int KEY_LENGTH_BYTES = 32;

    private static final int IV_LENGTH_BYTES = 12;

    private static final int AUTH_TAG_LENGTH_BITS = 128;

    private static final int MINIMUM_PAYLOAD_BYTES =
        IV_LENGTH_BYTES
            + AUTH_TAG_LENGTH_BITS / Byte.SIZE;

    private final SecretKey secretKey;

    private final SecureRandom secureRandom;

    public AesGcmEncryptionService(
        @Value("${api.security.encryption.key}")
            String encodedKey
    ) {
        this.secretKey = criarChave(encodedKey);
        this.secureRandom = new SecureRandom();
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(
                TRANSFORMATION
            );

            cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKey,
                new GCMParameterSpec(
                    AUTH_TAG_LENGTH_BITS,
                    iv
                )
            );

            byte[] encrypted = cipher.doFinal(
                plaintext.getBytes(
                    StandardCharsets.UTF_8
                )
            );

            byte[] payload = ByteBuffer
                .allocate(iv.length + encrypted.length)
                .put(iv)
                .put(encrypted)
                .array();

            return PREFIX
                + Base64.getEncoder()
                    .encodeToString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Não foi possível criptografar "
                    + "o dado sensível",
                exception
            );
        }
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null) {
            return null;
        }

        if (!isEncrypted(encryptedValue)) {
            throw new IllegalArgumentException(
                "O conteúdo não utiliza o formato "
                    + "de criptografia esperado"
            );
        }

        try {
            byte[] payload = Base64
                .getDecoder()
                .decode(
                    encryptedValue.substring(
                        PREFIX.length()
                    )
                );

            if (
                payload.length
                    < MINIMUM_PAYLOAD_BYTES
            ) {
                throw new IllegalArgumentException(
                    "Conteúdo criptografado inválido"
                );
            }

            byte[] iv = Arrays.copyOfRange(
                payload,
                0,
                IV_LENGTH_BYTES
            );

            byte[] ciphertext = Arrays.copyOfRange(
                payload,
                IV_LENGTH_BYTES,
                payload.length
            );

            Cipher cipher = Cipher.getInstance(
                TRANSFORMATION
            );

            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                new GCMParameterSpec(
                    AUTH_TAG_LENGTH_BITS,
                    iv
                )
            );

            byte[] plaintext = cipher.doFinal(
                ciphertext
            );

            return new String(
                plaintext,
                StandardCharsets.UTF_8
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Dado sensível criptografado "
                    + "inválido ou adulterado",
                exception
            );
        }
    }

    public boolean isEncrypted(String value) {
        return value != null
            && value.startsWith(PREFIX);
    }

    private SecretKey criarChave(
        String encodedKey
    ) {
        if (
            encodedKey == null
                || encodedKey.isBlank()
        ) {
            throw new IllegalStateException(
                "DATA_ENCRYPTION_KEY não foi configurada"
            );
        }

        final byte[] keyBytes;

        try {
            keyBytes = Base64
                .getDecoder()
                .decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                "DATA_ENCRYPTION_KEY deve estar "
                    + "codificada em Base64",
                exception
            );
        }

        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                "DATA_ENCRYPTION_KEY deve representar "
                    + "exatamente 32 bytes"
            );
        }

        return new SecretKeySpec(
            keyBytes,
            KEY_ALGORITHM
        );
    }
}