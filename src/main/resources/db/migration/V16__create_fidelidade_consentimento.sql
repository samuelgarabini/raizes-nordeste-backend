-- Registra a finalidade e a base legal da participação
-- no programa de fidelidade, permitindo concessão e revogação.
CREATE TABLE consentimentos_fidelidade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL UNIQUE,
    concedido BOOLEAN NOT NULL DEFAULT FALSE,
    versao_termo VARCHAR(50) NOT NULL,
    finalidade VARCHAR(50) NOT NULL
        DEFAULT 'PROGRAMA_FIDELIDADE',
    base_legal VARCHAR(30) NOT NULL
        DEFAULT 'CONSENTIMENTO',
    concedido_em TIMESTAMP WITH TIME ZONE,
    revogado_em TIMESTAMP WITH TIME ZONE,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_consentimento_fidelidade_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_consentimento_fidelidade_finalidade
        CHECK (finalidade = 'PROGRAMA_FIDELIDADE'),

    CONSTRAINT ck_consentimento_fidelidade_base_legal
        CHECK (base_legal = 'CONSENTIMENTO'),

    CONSTRAINT ck_consentimento_fidelidade_datas
        CHECK (
            (concedido = TRUE AND concedido_em IS NOT NULL
                AND revogado_em IS NULL)
            OR
            (concedido = FALSE)
        )
);

CREATE INDEX idx_consentimento_fidelidade_ativo
    ON consentimentos_fidelidade (
        cliente_id,
        concedido
    );

-- O usuário CLIENTE passa a ter vínculo explícito com
-- o cadastro ao qual pode acessar saldo, histórico e consentimento.
ALTER TABLE tb_usuarios
    ADD COLUMN cliente_id UUID;

UPDATE tb_usuarios
SET cliente_id =
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
WHERE LOWER(username) = 'cliente';

ALTER TABLE tb_usuarios
    ADD CONSTRAINT fk_usuarios_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes (id),
    ADD CONSTRAINT ck_usuarios_vinculo_cliente
        CHECK (
            (perfil = 'CLIENTE' AND cliente_id IS NOT NULL)
            OR
            (perfil <> 'CLIENTE' AND cliente_id IS NULL)
        );

CREATE UNIQUE INDEX uk_usuarios_cliente
    ON tb_usuarios (cliente_id)
    WHERE cliente_id IS NOT NULL;

-- Cada movimentação recebe um UUID próprio. Créditos continuam
-- vinculados ao pedido; débitos de resgate não exigem pedido.
ALTER TABLE historico_pontos
    ADD COLUMN operacao_id UUID;

UPDATE historico_pontos
SET operacao_id = gen_random_uuid()
WHERE operacao_id IS NULL;

ALTER TABLE historico_pontos
    ALTER COLUMN operacao_id SET NOT NULL,
    ALTER COLUMN operacao_id
        SET DEFAULT gen_random_uuid(),
    ALTER COLUMN pedido_id DROP NOT NULL;

ALTER TABLE historico_pontos
    DROP CONSTRAINT uk_historico_pontos_pedido_operacao;

ALTER TABLE historico_pontos
    ADD CONSTRAINT uk_historico_pontos_operacao
        UNIQUE (operacao_id);

CREATE UNIQUE INDEX uk_historico_pontos_pedido_operacao
    ON historico_pontos (
        pedido_id,
        tipo_operacao
    )
    WHERE pedido_id IS NOT NULL;

-- O cliente de demonstração já participava do programa e possuía
-- saldo inicial. O consentimento é explicitado para preservar o fluxo.
INSERT INTO consentimentos_fidelidade (
    cliente_id,
    concedido,
    versao_termo,
    concedido_em
)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    TRUE,
    '1.0',
    CURRENT_TIMESTAMP
);
