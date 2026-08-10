-- Alinha o esquema criado pelas migrations anteriores com as entidades JPA.

-- Pedido: a entidade Java utiliza valor_total.
ALTER TABLE tb_pedidos
    RENAME COLUMN total TO valor_total;

-- A forma de pagamento será preenchida quando o fluxo de pagamento
-- for implementado. Por enquanto, não pode impedir a criação do pedido.
ALTER TABLE tb_pedidos
    ALTER COLUMN forma_pagamento DROP NOT NULL;

ALTER TABLE tb_pedidos
    ADD CONSTRAINT ck_pedidos_valor_total
        CHECK (valor_total >= 0);

ALTER TABLE tb_pedidos
    ADD CONSTRAINT ck_pedidos_canal
        CHECK (canal_pedido IN ('APP', 'TOTEM', 'BALCAO', 'PICKUP', 'WEB'));

-- Cliente: adiciona o CPF esperado pela entidade Java.
ALTER TABLE clientes
    ADD COLUMN cpf VARCHAR(255);

-- Preenche os registros de demonstração criados pela V4.
UPDATE clientes
SET cpf = 'DEMO-' || id::text
WHERE cpf IS NULL;

ALTER TABLE clientes
    ALTER COLUMN nome SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN cpf SET NOT NULL;

CREATE UNIQUE INDEX uk_clientes_cpf
    ON clientes (cpf);

-- Relacionamento entre pedido e cliente.
ALTER TABLE tb_pedidos
    ADD CONSTRAINT fk_pedidos_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes (id);

-- Tabelas exigidas pelas entidades Categoria e Produto.
CREATE TABLE tb_categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    ordem_exibicao INTEGER
);

CREATE TABLE tb_produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao VARCHAR(500),
    preco NUMERIC(10, 2) NOT NULL,
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    unidade_id UUID NOT NULL,
    categoria_id BIGINT,

    CONSTRAINT ck_produtos_preco
        CHECK (preco >= 0),

    CONSTRAINT fk_produtos_unidade
        FOREIGN KEY (unidade_id)
        REFERENCES tb_unidades (id),

    CONSTRAINT fk_produtos_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES tb_categorias (id)
);

CREATE INDEX idx_produtos_unidade
    ON tb_produtos (unidade_id);

CREATE INDEX idx_produtos_categoria
    ON tb_produtos (categoria_id);