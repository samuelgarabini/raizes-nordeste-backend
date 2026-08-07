-- Tabela de Campanhas Promocionais
CREATE TABLE campanhas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    codigo_promocional VARCHAR(50) UNIQUE,
    desconto_percentual DECIMAL(5,2),
    valor_minimo_pedido DECIMAL(10,2) DEFAULT 0.00,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

-- Tabela de Carteira de Fidelidade do Cliente
CREATE TABLE carteiras_fidelidade (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL UNIQUE, -- Assumindo que a tabela de clientes já existe
    pontos_acumulados INT DEFAULT 0,
    ultima_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cliente_fidelidade FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);

-- Histórico de transações de pontos (para auditoria)
CREATE TABLE historico_pontos (
    id BIGSERIAL PRIMARY KEY,
    carteira_id BIGINT NOT NULL,
    pedido_id BIGINT NOT NULL, -- Referência ao pedido que gerou/usou os pontos
    pontos_alterados INT NOT NULL, -- Positivo (ganho) ou Negativo (resgate)
    tipo_operacao VARCHAR(20) NOT NULL, -- 'ACUMULO' ou 'RESGATE'
    data_operacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carteira_historico FOREIGN KEY (carteira_id) REFERENCES carteiras_fidelidade(id),
    CONSTRAINT fk_pedido_historico FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
);