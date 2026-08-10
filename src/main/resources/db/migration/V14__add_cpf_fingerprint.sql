-- A criptografia AES-GCM utiliza IV aleatório.
-- Portanto, a unicidade do CPF deve ser garantida
-- por uma impressão HMAC determinística.

ALTER TABLE clientes
    ADD COLUMN cpf_fingerprint VARCHAR(64);

DROP INDEX IF EXISTS uk_clientes_cpf;

CREATE UNIQUE INDEX uk_clientes_cpf_fingerprint
    ON clientes (cpf_fingerprint)
    WHERE cpf_fingerprint IS NOT NULL;

ALTER TABLE clientes
    ADD CONSTRAINT ck_clientes_cpf_fingerprint
        CHECK (
            cpf_fingerprint IS NULL
            OR cpf_fingerprint
                ~ '^[0-9a-f]{64}$'
        );