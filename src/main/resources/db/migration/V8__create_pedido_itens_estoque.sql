-- Permite garantir que o estoque pertença à mesma unidade do produto.
ALTER TABLE tb_produtos
    ADD CONSTRAINT uk_produtos_id_unidade
        UNIQUE (id, unidade_id);

-- Saldo disponível de cada produto em cada unidade.
CREATE TABLE tb_estoques (
    id BIGSERIAL PRIMARY KEY,
    unidade_id UUID NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL DEFAULT 0,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_estoques_unidade_produto
        UNIQUE (unidade_id, produto_id),

    CONSTRAINT ck_estoques_quantidade
        CHECK (quantidade >= 0),

    CONSTRAINT fk_estoques_produto_unidade
        FOREIGN KEY (produto_id, unidade_id)
        REFERENCES tb_produtos (id, unidade_id)
);

-- Produtos e quantidades que compõem cada pedido.
CREATE TABLE tb_pedido_itens (
    id BIGSERIAL PRIMARY KEY,
    pedido_id UUID NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,

    CONSTRAINT uk_pedido_itens_pedido_produto
        UNIQUE (pedido_id, produto_id),

    CONSTRAINT ck_pedido_itens_quantidade
        CHECK (quantidade > 0),

    CONSTRAINT ck_pedido_itens_preco
        CHECK (preco_unitario >= 0),

    CONSTRAINT ck_pedido_itens_subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT fk_pedido_itens_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES tb_pedidos (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pedido_itens_produto
        FOREIGN KEY (produto_id)
        REFERENCES tb_produtos (id)
);

CREATE INDEX idx_estoques_unidade
    ON tb_estoques (unidade_id);

CREATE INDEX idx_estoques_produto
    ON tb_estoques (produto_id);

CREATE INDEX idx_pedido_itens_pedido
    ON tb_pedido_itens (pedido_id);

CREATE INDEX idx_pedido_itens_produto
    ON tb_pedido_itens (produto_id);

-- Estoque inicial dos produtos de demonstração.
-- Produtos disponíveis recebem 50 unidades.
-- Produtos indisponíveis permanecem com estoque zero.
INSERT INTO tb_estoques (
    unidade_id,
    produto_id,
    quantidade
)
SELECT
    unidade_id,
    id,
    CASE
        WHEN disponivel = TRUE THEN 50
        ELSE 0
    END
FROM tb_produtos
ON CONFLICT (unidade_id, produto_id) DO NOTHING;