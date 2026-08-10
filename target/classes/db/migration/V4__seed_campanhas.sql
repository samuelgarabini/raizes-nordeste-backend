-- Inserir campanhas promocionais
INSERT INTO campanhas (nome, codigo_promocional, desconto_percentual, valor_minimo_pedido, data_inicio, data_fim, ativo)
VALUES 
('Inauguração Raízes Nordeste', 'BEMVINDO10', 10.00, 50.00, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', true),
('Sextou com Frete Grátis', 'SEXTOU', 5.00, 100.00, CURRENT_DATE, CURRENT_DATE + INTERVAL '7 days', true);

-- Inserir um cliente de testes principal (com UUID) caso ele não exista
INSERT INTO clientes (id, nome, email) 
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Cliente Teste', 'teste@email.com')
ON CONFLICT (id) DO NOTHING;

-- Associar uma carteira de fidelidade utilizando o mesmo UUID do cliente
INSERT INTO carteiras_fidelidade (cliente_id, pontos_acumulados)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 50);