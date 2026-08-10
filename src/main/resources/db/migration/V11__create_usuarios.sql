CREATE TABLE tb_usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL,
    senha_hash VARCHAR(60) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL
        DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_usuarios_perfil
        CHECK (
            perfil IN (
                'ADMIN',
                'GERENTE',
                'ATENDENTE',
                'CLIENTE'
            )
        ),

    CONSTRAINT ck_usuarios_username
        CHECK (LENGTH(TRIM(username)) >= 3),

    CONSTRAINT ck_usuarios_senha_hash
        CHECK (LENGTH(senha_hash) = 60)
);

CREATE UNIQUE INDEX uk_usuarios_username_lower
    ON tb_usuarios (LOWER(username));

CREATE INDEX idx_usuarios_perfil
    ON tb_usuarios (perfil);

CREATE INDEX idx_usuarios_ativo
    ON tb_usuarios (ativo);