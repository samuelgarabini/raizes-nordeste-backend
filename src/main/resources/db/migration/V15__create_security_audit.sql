CREATE TABLE tb_auditoria_seguranca (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    evento VARCHAR(50) NOT NULL,
    resultado VARCHAR(20) NOT NULL,

    ator_fingerprint VARCHAR(64),
    perfil VARCHAR(20),

    recurso VARCHAR(50),
    recurso_id VARCHAR(100),

    ip_fingerprint VARCHAR(64),
    codigo_erro VARCHAR(100),

    ocorrido_em TIMESTAMP WITH TIME ZONE NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_auditoria_resultado
        CHECK (
            resultado IN (
                'SUCESSO',
                'FALHA'
            )
        ),

    CONSTRAINT ck_auditoria_perfil
        CHECK (
            perfil IS NULL
            OR perfil IN (
                'ADMIN',
                'GERENTE',
                'ATENDENTE',
                'CLIENTE'
            )
        ),

    CONSTRAINT ck_auditoria_ator_fingerprint
        CHECK (
            ator_fingerprint IS NULL
            OR ator_fingerprint
                ~ '^[0-9a-f]{64}$'
        ),

    CONSTRAINT ck_auditoria_ip_fingerprint
        CHECK (
            ip_fingerprint IS NULL
            OR ip_fingerprint
                ~ '^[0-9a-f]{64}$'
        )
);

CREATE INDEX idx_auditoria_evento_ocorrido
    ON tb_auditoria_seguranca (
        evento,
        ocorrido_em DESC
    );

CREATE INDEX idx_auditoria_ator_ocorrido
    ON tb_auditoria_seguranca (
        ator_fingerprint,
        ocorrido_em DESC
    );

CREATE INDEX idx_auditoria_recurso
    ON tb_auditoria_seguranca (
        recurso,
        recurso_id
    );