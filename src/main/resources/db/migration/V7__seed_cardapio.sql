-- Categorias utilizadas pelos cardápios das unidades.
INSERT INTO tb_categorias (id, nome, ordem_exibicao)
VALUES
    (1, 'Entradas', 1),
    (2, 'Pratos Principais', 2),
    (3, 'Bebidas', 3),
    (4, 'Sobremesas', 4)
ON CONFLICT (id) DO NOTHING;

-- Cardápio da unidade Recife Antigo.
INSERT INTO tb_produtos (
    id,
    nome,
    descricao,
    preco,
    disponivel,
    unidade_id,
    categoria_id
)
VALUES
    (
        101,
        'Cuscuz com Carne de Sol',
        'Cuscuz nordestino acompanhado de carne de sol.',
        24.90,
        TRUE,
        '550e8400-e29b-41d4-a716-446655440000',
        2
    ),
    (
        102,
        'Baião de Dois',
        'Arroz, feijão-verde, queijo coalho e carne de sol.',
        32.90,
        TRUE,
        '550e8400-e29b-41d4-a716-446655440000',
        2
    ),
    (
        103,
        'Cartola',
        'Banana, queijo, açúcar e canela.',
        16.90,
        TRUE,
        '550e8400-e29b-41d4-a716-446655440000',
        4
    ),
    (
        104,
        'Suco de Caju',
        'Suco natural de caju.',
        8.50,
        TRUE,
        '550e8400-e29b-41d4-a716-446655440000',
        3
    ),
    (
        105,
        'Produto Indisponível',
        'Produto utilizado para testar o filtro de disponibilidade.',
        10.00,
        FALSE,
        '550e8400-e29b-41d4-a716-446655440000',
        1
    )
ON CONFLICT (id) DO NOTHING;

-- Cardápio da unidade Salvador Barra.
INSERT INTO tb_produtos (
    id,
    nome,
    descricao,
    preco,
    disponivel,
    unidade_id,
    categoria_id
)
VALUES
    (
        201,
        'Acarajé',
        'Acarajé baiano com vatapá e salada.',
        22.90,
        TRUE,
        'a1b2c3d4-e5f6-7890-abcd-ef1234567891',
        1
    ),
    (
        202,
        'Moqueca Baiana',
        'Moqueca acompanhada de arroz e farofa.',
        44.90,
        TRUE,
        'a1b2c3d4-e5f6-7890-abcd-ef1234567891',
        2
    ),
    (
        203,
        'Cocada',
        'Cocada tradicional.',
        12.00,
        TRUE,
        'a1b2c3d4-e5f6-7890-abcd-ef1234567891',
        4
    ),
    (
        204,
        'Suco de Umbu',
        'Suco natural de umbu.',
        9.00,
        TRUE,
        'a1b2c3d4-e5f6-7890-abcd-ef1234567891',
        3
    )
ON CONFLICT (id) DO NOTHING;

-- Reposiciona as sequências após os IDs explícitos dos dados iniciais.
SELECT setval(
    pg_get_serial_sequence('tb_categorias', 'id'),
    (SELECT MAX(id) FROM tb_categorias),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('tb_produtos', 'id'),
    (SELECT MAX(id) FROM tb_produtos),
    TRUE
);