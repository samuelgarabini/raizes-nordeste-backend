CREATE TABLE tb_pagamentos (
    id UUID PRIMARY KEY,

    pedido_id UUID NOT NULL,

    transacao_id UUID NOT NULL,

    valor DECIMAL(10, 2) NOT NULL,

    status VARCHAR(20) NOT NULL,

    gateway VARCHAR(50) NOT NULL
        DEFAULT 'MOCK_GATEWAY',

    motivo_recusa VARCHAR(255),

    processado_em TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pagamentos_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES tb_pedidos (id),

    CONSTRAINT uk_pagamentos_pedido
        UNIQUE (pedido_id),

    CONSTRAINT uk_pagamentos_transacao
        UNIQUE (transacao_id),

    CONSTRAINT ck_pagamentos_valor
        CHECK (valor >= 0),

    CONSTRAINT ck_pagamentos_status
        CHECK (
            status IN (
                'APROVADO',
                'RECUSADO'
            )
        ),

    CONSTRAINT ck_pagamentos_motivo_recusa
        CHECK (
            status = 'RECUSADO'
            OR motivo_recusa IS NULL
        )
);

CREATE INDEX idx_pagamentos_status
    ON tb_pagamentos (status);

CREATE INDEX idx_pagamentos_processado_em
    ON tb_pagamentos (processado_em);