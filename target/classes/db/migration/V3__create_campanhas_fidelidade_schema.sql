-- Tabela de Campanhas Promocionais
CREATE TABLE IF NOT EXISTS campanhas (
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
-- (Garante que a tabela clientes exista ou cria uma básica se necessário)
CREATE TABLE IF NOT EXISTS clientes (
    id UUID PRIMARY KEY,
    nome VARCHAR(150),
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS carteiras_fidelidade (
    id BIGSERIAL PRIMARY KEY,
    cliente_id UUID NOT NULL UNIQUE, 
    pontos_acumulados INT DEFAULT 0,
    ultima_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cliente_fidelidade FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);

-- Histórico de transações de pontos (para auditoria)
-- Nota: A FK de pedidos foi retirada temporariamente caso a tabela pedidos ainda não exista nas migrations
CREATE TABLE IF NOT EXISTS historico_pontos (
    id BIGSERIAL PRIMARY KEY,
    carteira_id BIGINT NOT NULL,
    pedido_id BIGINT NOT NULL, 
    pontos_alterados INT NOT NULL, 
    tipo_operacao VARCHAR(20) NOT NULL, 
    data_operacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carteira_historico FOREIGN KEY (carteira_id) REFERENCES carteiras_fidelidade(id)
);