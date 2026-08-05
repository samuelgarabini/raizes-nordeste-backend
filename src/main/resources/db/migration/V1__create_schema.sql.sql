CREATE TABLE tb_unidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    codigo_loja VARCHAR(20) UNIQUE NOT NULL,
    ativa BOOLEAN DEFAULT TRUE
);

CREATE TABLE tb_pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidade_id UUID NOT NULL REFERENCES tb_unidades(id),
    cliente_id UUID NOT NULL,
    canal_pedido VARCHAR(20) NOT NULL,
    data_hora TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    total NUMERIC(10,2) NOT NULL,
    forma_pagamento VARCHAR(30) NOT NULL,
    observacao TEXT
);

ALTER TABLE tb_pedidos ENABLE ROW LEVEL SECURITY;

CREATE POLICY pedidos_tenant_isolation ON tb_pedidos
    FOR ALL
    USING (unidade_id = NULLIF(current_setting('app.current_store_id', true), '')::uuid);