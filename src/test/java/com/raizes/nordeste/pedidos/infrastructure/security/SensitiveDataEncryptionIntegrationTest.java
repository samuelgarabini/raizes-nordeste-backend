package com.raizes.nordeste.pedidos.infrastructure.security;

import com.raizes.nordeste.pedidos.domain.Cliente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SensitiveDataEncryptionIntegrationTest {

    private static final String PREFIX = "ENC:v1:";

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LegacySensitiveDataMigration
        legacyDataMigration;

    @Test
    void devePersistirDadosCriptografados()
        throws Exception {

        UUID clienteId = UUID.randomUUID();

        Cliente cliente = criarCliente(
            clienteId,
            "Cliente Criptografado",
            "12345678901",
            "cliente@teste.com"
        );

        entityManager.persist(cliente);
        entityManager.flush();

        String storedCpf =
            buscarColuna(clienteId, "cpf");

        String storedEmail =
            buscarColuna(clienteId, "email");

        String storedFingerprint =
            buscarColuna(
                clienteId,
                "cpf_fingerprint"
            );

        assertThat(storedCpf)
            .startsWith(PREFIX)
            .doesNotContain("12345678901");

        assertThat(storedEmail)
            .startsWith(PREFIX)
            .doesNotContain("cliente@teste.com");

        assertThat(storedFingerprint)
            .hasSize(64)
            .matches("[0-9a-f]{64}")
            .doesNotContain("12345678901");

        entityManager.clear();

        Cliente restoredCliente =
            entityManager.find(
                Cliente.class,
                clienteId
            );

        assertThat(restoredCliente.getCpf())
            .isEqualTo("12345678901");

        assertThat(restoredCliente.getEmail())
            .isEqualTo("cliente@teste.com");
    }

    @Test
    void deveMigrarDadosLegados()
        throws Exception {

        UUID clienteId = UUID.randomUUID();

        jdbcTemplate.update(
            """
                INSERT INTO clientes (
                    id,
                    nome,
                    cpf,
                    email,
                    cpf_fingerprint
                )
                VALUES (?, ?, ?, ?, NULL)
                """,
            clienteId,
            "Cliente Legado",
            "98765432100",
            "legado@teste.com"
        );

        legacyDataMigration.run(null);

        assertThat(
            buscarColuna(clienteId, "cpf")
        ).startsWith(PREFIX);

        assertThat(
            buscarColuna(clienteId, "email")
        ).startsWith(PREFIX);

        assertThat(
            buscarColuna(
                clienteId,
                "cpf_fingerprint"
            )
        )
            .hasSize(64)
            .matches("[0-9a-f]{64}");

        entityManager.clear();

        Cliente restoredCliente =
            entityManager.find(
                Cliente.class,
                clienteId
            );

        assertThat(restoredCliente.getCpf())
            .isEqualTo("98765432100");

        assertThat(restoredCliente.getEmail())
            .isEqualTo("legado@teste.com");
    }

    @Test
    void deveImpedirCpfDuplicadoMesmoFormatado()
        throws Exception {

        Cliente firstCliente = criarCliente(
            UUID.randomUUID(),
            "Primeiro Cliente",
            "12345678901",
            "primeiro@teste.com"
        );

        entityManager.persist(firstCliente);
        entityManager.flush();
        entityManager.clear();

        Cliente secondCliente = criarCliente(
            UUID.randomUUID(),
            "Segundo Cliente",
            "123.456.789-01",
            "segundo@teste.com"
        );

        entityManager.persist(secondCliente);

        assertThatThrownBy(entityManager::flush)
            .isInstanceOf(PersistenceException.class);
    }

    private Cliente criarCliente(
        UUID id,
        String nome,
        String cpf,
        String email
    ) {
        Cliente cliente = new Cliente();

        cliente.setId(id);
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setEmail(email);

        return cliente;
    }

    private String buscarColuna(
        UUID clienteId,
        String coluna
    ) {
        String sql = """
            SELECT %s
            FROM clientes
            WHERE id = ?
            """.formatted(coluna);

        return jdbcTemplate.queryForObject(
            sql,
            String.class,
            clienteId
        );
    }
}