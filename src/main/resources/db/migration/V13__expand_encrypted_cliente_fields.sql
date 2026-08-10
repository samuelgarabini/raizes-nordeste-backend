-- AES-GCM acrescenta IV, tag de autenticação e codificação Base64.
-- As colunas precisam comportar o conteúdo criptografado.

ALTER TABLE clientes
    ALTER COLUMN cpf TYPE VARCHAR(1024);

ALTER TABLE clientes
    ALTER COLUMN email TYPE VARCHAR(1024);