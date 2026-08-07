INSERT INTO campanhas (nome, codigo_promocional, desconto_percentual, valor_minimo_pedido, data_inicio, data_fim, ativo)
VALUES 
('Inauguração Raízes Nordeste', 'BEMVINDO10', 10.00, 50.00, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', true),
('Sextou com Frete Grátis', 'SEXTOU', 5.00, 100.00, CURRENT_DATE, CURRENT_DATE + INTERVAL '7 days', true);

-- Associar uma carteira vazia para o usuário de testes principal (id=1)
INSERT INTO carteiras_fidelidade (cliente_id, pontos_acumulados)
VALUES (1, 50);