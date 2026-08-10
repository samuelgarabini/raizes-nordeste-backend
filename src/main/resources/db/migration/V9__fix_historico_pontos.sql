-- A aplicação anterior não gravava registros nesta tabela.
-- A migration interrompe com segurança caso encontre dados inesperados,
-- evitando qualquer perda silenciosa.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM historico_pontos) THEN
        RAISE EXCEPTION
            'A tabela historico_pontos contém registros legados. '
            'É necessária uma migração manual antes de converter pedido_id.';
    END IF;
END
$$;

-- O identificador de tb_pedidos é UUID, não BIGINT.
ALTER TABLE historico_pontos
    DROP COLUMN pedido_id;

ALTER TABLE historico_pontos
    ADD COLUMN pedido_id UUID NOT NULL;

ALTER TABLE historico_pontos
    ALTER COLUMN data_operacao SET NOT NULL;

ALTER TABLE historico_pontos
    ALTER COLUMN data_operacao
        SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE historico_pontos
    ADD CONSTRAINT fk_historico_pontos_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES tb_pedidos (id);

ALTER TABLE historico_pontos
    ADD CONSTRAINT uk_historico_pontos_pedido_operacao
        UNIQUE (pedido_id, tipo_operacao);

ALTER TABLE historico_pontos
    ADD CONSTRAINT ck_historico_pontos_quantidade
        CHECK (pontos_alterados >= 0);

ALTER TABLE historico_pontos
    ADD CONSTRAINT ck_historico_pontos_tipo
        CHECK (
            tipo_operacao IN (
                'CREDITO',
                'DEBITO',
                'ESTORNO'
            )
        );

CREATE INDEX idx_historico_pontos_carteira
    ON historico_pontos (carteira_id);

CREATE INDEX idx_historico_pontos_data
    ON historico_pontos (data_operacao);