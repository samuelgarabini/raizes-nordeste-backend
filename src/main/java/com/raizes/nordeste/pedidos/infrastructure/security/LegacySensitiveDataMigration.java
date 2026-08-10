package com.raizes.nordeste.pedidos.infrastructure.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class LegacySensitiveDataMigration
    implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    private final AesGcmEncryptionService
        encryptionService;

    private final SensitiveDataFingerprintService
        fingerprintService;

    public LegacySensitiveDataMigration(
        JdbcTemplate jdbcTemplate,
        AesGcmEncryptionService encryptionService,
        SensitiveDataFingerprintService
            fingerprintService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.encryptionService = encryptionService;
        this.fingerprintService = fingerprintService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        List<ClienteLegacy> clientes =
            buscarClientesParaMigracao();

        for (ClienteLegacy cliente : clientes) {
            migrarCliente(cliente);
        }
    }

    private void migrarCliente(
        ClienteLegacy cliente
    ) {
        String cpfPlaintext =
            obterPlaintext(cliente.cpf());

        String emailPlaintext =
            obterPlaintext(cliente.email());

        String encryptedCpf =
            criptografarSeNecessario(
                cliente.cpf(),
                cpfPlaintext
            );

        String encryptedEmail =
            criptografarSeNecessario(
                cliente.email(),
                emailPlaintext
            );

        String cpfFingerprint =
            fingerprintService.fingerprintCpf(
                cpfPlaintext
            );

        jdbcTemplate.update(
            """
                UPDATE clientes
                SET cpf = ?,
                    email = ?,
                    cpf_fingerprint = ?
                WHERE id = ?
                """,
            encryptedCpf,
            encryptedEmail,
            cpfFingerprint,
            cliente.id()
        );
    }

    private List<ClienteLegacy>
        buscarClientesParaMigracao() {

        return jdbcTemplate.query(
            """
                SELECT
                    id,
                    cpf,
                    email,
                    cpf_fingerprint
                FROM clientes
                WHERE cpf_fingerprint IS NULL
                   OR (
                       cpf IS NOT NULL
                       AND cpf NOT LIKE 'ENC:v1:%'
                   )
                   OR (
                       email IS NOT NULL
                       AND email NOT LIKE 'ENC:v1:%'
                   )
                """,
            (resultSet, rowNumber) ->
                new ClienteLegacy(
                    resultSet.getObject(
                        "id",
                        UUID.class
                    ),
                    resultSet.getString("cpf"),
                    resultSet.getString("email"),
                    resultSet.getString(
                        "cpf_fingerprint"
                    )
                )
        );
    }

    private String obterPlaintext(String value) {
        if (value == null) {
            return null;
        }

        if (encryptionService.isEncrypted(value)) {
            return encryptionService.decrypt(value);
        }

        return value;
    }

    private String criptografarSeNecessario(
        String originalValue,
        String plaintext
    ) {
        if (originalValue == null) {
            return null;
        }

        if (
            encryptionService.isEncrypted(
                originalValue
            )
        ) {
            return originalValue;
        }

        return encryptionService.encrypt(plaintext);
    }

    private record ClienteLegacy(
        UUID id,
        String cpf,
        String email,
        String cpfFingerprint
    ) {
    }
}