-- A senha de demonstração de todos os usuários é:
-- Senha@123
--
-- Somente o hash BCrypt é armazenado no banco.

INSERT INTO tb_usuarios (
    id,
    username,
    senha_hash,
    perfil,
    ativo
)
VALUES
    (
        '10000000-0000-0000-0000-000000000001',
        'admin',
        '$2b$12$bQvttdBYw2KsXCaqWEA10uUjll6TmI.lhldAVb33Vvp2VWnsLu8I6',
        'ADMIN',
        TRUE
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        'gerente',
        '$2b$12$bQvttdBYw2KsXCaqWEA10uUjll6TmI.lhldAVb33Vvp2VWnsLu8I6',
        'GERENTE',
        TRUE
    ),
    (
        '10000000-0000-0000-0000-000000000003',
        'atendente',
        '$2b$12$bQvttdBYw2KsXCaqWEA10uUjll6TmI.lhldAVb33Vvp2VWnsLu8I6',
        'ATENDENTE',
        TRUE
    ),
    (
        '10000000-0000-0000-0000-000000000004',
        'cliente',
        '$2b$12$bQvttdBYw2KsXCaqWEA10uUjll6TmI.lhldAVb33Vvp2VWnsLu8I6',
        'CLIENTE',
        TRUE
    );