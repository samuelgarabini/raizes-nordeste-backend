# Rede Raízes do Nordeste — Back-End API

[![Continuous Integration](https://github.com/samuelgarabini/raizes-nordeste-backend/actions/workflows/ci.yaml/badge.svg)](https://github.com/samuelgarabini/raizes-nordeste-backend/actions/workflows/ci.yaml)

API REST desenvolvida para o projeto multidisciplinar da rede de lanchonetes **Raízes do Nordeste**.

A aplicação implementa autenticação JWT, cardápio por unidade, pedidos multicanal, controle de estoque, checkout com pagamento simulado, campanhas promocionais, fidelidade, ciclo de status, proteção de dados pessoais e auditoria de operações sensíveis.

## Tecnologias

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA e Hibernate
- Spring Security
- JWT
- PostgreSQL 15
- Flyway
- OpenAPI 3 e Swagger UI
- JUnit 5, MockMvc e AssertJ
- Docker e Docker Compose
- GitHub Actions

O Docker Compose também provisiona Redis, RabbitMQ, Kafka e Kafka UI para evolução da arquitetura. O funcionamento correto do fluxo principal, entretanto, não depende atualmente desses componentes.

## Funcionalidades implementadas

- Autenticação com usuários persistidos no PostgreSQL.
- Senhas armazenadas com BCrypt.
- Emissão e validação de tokens JWT.
- Autorização baseada nos perfis `ADMIN`, `GERENTE`, `ATENDENTE` e `CLIENTE`.
- Consulta pública do cardápio por unidade.
- Criação de pedidos com os canais:
  - `APP`
  - `TOTEM`
  - `BALCAO`
  - `PICKUP`
  - `WEB`
- Validação de unidade, cliente, produtos, quantidades e estoque.
- Cálculo do preço no servidor.
- Persistência dos itens e valores do pedido.
- Consulta detalhada de pedido.
- Listagem paginada com filtros por canal, status e unidade.
- Checkout idempotente.
- Pagamento mock aprovado ou recusado.
- Aplicação de campanha promocional.
- Baixa e restauração transacional de estoque.
- Consentimento explícito e revogável para o programa de fidelidade.
- Crédito de pontos condicionado ao consentimento ativo.
- Consulta de saldo e histórico de movimentações.
- Resgate transacional de pontos com validação de saldo.
- Autorização por objeto: o perfil `CLIENTE` acessa apenas a própria carteira.
- Atualização controlada do ciclo de status do pedido.
- Cancelamento controlado antes do pagamento, com devolução transacional do estoque.
- Respostas de erro padronizadas.
- Criptografia AES-256-GCM dos dados pessoais.
- Impressão digital HMAC-SHA-256 para busca e unicidade do CPF.
- Auditoria de login, checkout, cancelamento, alteração de status, consulta, consentimento e resgate de fidelidade.
- Migrações e dados iniciais controlados pelo Flyway.
- Testes unitários e de integração automatizados.

## Arquitetura

O projeto adota uma organização em camadas:

```text
src/main/java/com/raizes/nordeste/
└── pedidos/
    ├── application/       Casos de uso e DTOs
    ├── domain/            Entidades e enums de domínio
    ├── service/           Serviços de regras de negócio
    ├── repository/        Interfaces de persistência
    ├── infrastructure/    Segurança, auditoria, web e detalhes técnicos
    ├── presentation/      Controladores e respostas da API
    └── controller/        Controladores de recursos complementares
```

As alterações do banco ficam em:

```text
src/main/resources/db/migration/
```

O Hibernate utiliza `ddl-auto: validate`. Portanto, o Flyway é responsável por criar e evoluir o esquema, enquanto o Hibernate verifica a compatibilidade entre as entidades e o banco.

## Pré-requisitos

Para executar todo o ambiente usando contêineres:

- Git
- Docker Desktop
- Docker Compose

Não é necessário instalar Java ou Maven para iniciar a aplicação pelo Docker.

## Configuração das variáveis de ambiente

Na raiz do projeto, copie o arquivo de exemplo:

```powershell
Copy-Item .env.example .env
```

Edite o `.env` e substitua os valores de demonstração.

As três chaves de segurança devem ser diferentes. Para gerar uma chave aleatória de 32 bytes em Base64 usando Docker, execute o comando abaixo três vezes:

```powershell
docker run --rm alpine:3.20 sh -c "head -c 32 /dev/urandom | base64"
```

Use uma saída diferente em cada variável:

```dotenv
JWT_SECRET=primeira_saida
DATA_ENCRYPTION_KEY=segunda_saida
DATA_FINGERPRINT_KEY=terceira_saida
```

## Inicialização da aplicação

Valide a configuração:

```powershell
docker compose config --quiet
$LASTEXITCODE
```

O resultado esperado é `0`.

Compile a imagem e inicie todos os serviços:

```powershell
docker compose up -d --build
```

Confira o estado dos contêineres:

```powershell
docker compose ps
```

Acompanhe os logs da API:

```powershell
docker compose logs -f api-backend
```

Use `Ctrl+C` para sair da visualização dos logs sem encerrar os contêineres.

## Portas locais

| Serviço | Endereço |
|---|---|
| API | `http://localhost:8081` |
| Swagger UI | `http://localhost:8081/swagger-ui/index.html` |
| Especificação OpenAPI | `http://localhost:8081/v3/api-docs` |
| PostgreSQL | `localhost:5433` |
| Redis | `localhost:6379` |
| RabbitMQ | `localhost:5672` |
| RabbitMQ Management | `http://localhost:15672` |
| Kafka | `localhost:9092` |
| Kafka UI | `http://localhost:8085` |

A porta externa da API pode ser alterada pela variável `API_PORT`.

## Usuários de demonstração

As migrações criam quatro usuários exclusivamente para testes locais:

| Usuário | Senha | Perfil |
|---|---|---|
| `admin` | `Senha@123` | `ADMIN` |
| `gerente` | `Senha@123` | `GERENTE` |
| `atendente` | `Senha@123` | `ATENDENTE` |
| `cliente` | `Senha@123` | `CLIENTE` |

Essas credenciais são dados de demonstração e não devem ser usadas em produção.

## Autenticação

Endpoint:

```http
POST /api/v1/auth/login
```

Exemplo:

```json
{
  "username": "cliente",
  "password": "Senha@123"
}
```

Resposta:

```json
{
  "token": "token-jwt",
  "tipo": "Bearer",
  "expiracaoEm": 86400000
}
```

Nas rotas protegidas, envie:

```http
Authorization: Bearer token-jwt
```

## Endpoints principais

| Método | Rota | Acesso | Finalidade |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Público | Autenticar e emitir JWT |
| `GET` | `/api/v1/unidades/{unidadeId}/cardapio` | Público | Consultar cardápio disponível |
| `GET` | `/api/campanhas` | Autenticado | Listar campanhas |
| `POST` | `/api/v1/pedidos` | Autenticado | Criar pedido |
| `GET` | `/api/v1/pedidos` | Autenticado | Listar e filtrar pedidos |
| `GET` | `/api/v1/pedidos/{id}` | Autenticado | Consultar detalhes |
| `POST` | `/api/v1/pedidos/{id}/checkout` | Autenticado | Processar pagamento mock |
| `PATCH` | `/api/v1/pedidos/{id}/status` | `ADMIN`, `GERENTE` ou `ATENDENTE` | Atualizar status |
| `PATCH` | `/api/v1/pedidos/{id}/cancelamento` | `ADMIN`, `GERENTE` ou `ATENDENTE` | Cancelar pedido antes do pagamento |
| `GET` | `/api/v1/fidelidade/{clienteId}` | Autenticado e autorizado para o cliente | Consultar consentimento, saldo e histórico |
| `PUT` | `/api/v1/fidelidade/{clienteId}/consentimento` | Próprio `CLIENTE` | Conceder ou revogar consentimento |
| `POST` | `/api/v1/fidelidade/{clienteId}/resgates` | Próprio `CLIENTE` | Resgatar pontos disponíveis |
| `GET` | `/api/v1/pedidos/health` | Autenticado | Verificar a API |

O Swagger apresenta o contrato navegável da aplicação. A coleção Postman fica em:

```text
docs/postman_collection.json
```

## Exemplo de criação de pedido

```http
POST /api/v1/pedidos
Authorization: Bearer token-jwt
Content-Type: application/json
```

```json
{
  "clienteId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "unidadeId": "550e8400-e29b-41d4-a716-446655440000",
  "canalPedido": "APP",
  "itens": [
    {
      "produtoId": 101,
      "quantidade": 2
    },
    {
      "produtoId": 104,
      "quantidade": 1
    }
  ]
}
```

Os preços não são recebidos do cliente. A API consulta os produtos no banco e calcula os valores no servidor.

## Checkout com pagamento mock

Exemplo de pagamento aprovado:

```http
POST /api/v1/pedidos/{id}/checkout?resultadoPagamento=APROVADO
```

Exemplo de pagamento recusado:

```http
POST /api/v1/pedidos/{id}/checkout?resultadoPagamento=RECUSADO
```

Aplicação opcional de cupom:

```http
POST /api/v1/pedidos/{id}/checkout?codigoPromocional=BEMVINDO10
```

O cupom `BEMVINDO10` concede 10% de desconto para pedidos que atendam ao valor mínimo configurado na campanha.

## Cancelamento de pedido

Pedidos em `AGUARDANDO_PAGAMENTO` podem ser cancelados por usuários com os perfis `ADMIN`, `GERENTE` ou `ATENDENTE`:

```http
PATCH /api/v1/pedidos/{id}/cancelamento
Authorization: Bearer token-jwt
```

O cancelamento altera o status para `CANCELADO`, devolve integralmente ao estoque os produtos reservados e registra a operação na auditoria. Pedidos pagos, recusados, em preparação, prontos, entregues ou já cancelados retornam `409 CANCELAMENTO_NAO_PERMITIDO`.

## Programa de fidelidade e consentimento

O usuário de demonstração `cliente` está vinculado ao cadastro `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11`. Esse vínculo impede que um cliente consulte ou altere a carteira de outra pessoa.

Consulta do saldo, consentimento e histórico:

```http
GET /api/v1/fidelidade/{clienteId}
Authorization: Bearer token-jwt
```

Concessão ou revogação explícita do consentimento:

```http
PUT /api/v1/fidelidade/{clienteId}/consentimento
Authorization: Bearer token-jwt
Content-Type: application/json
```

```json
{
  "concedido": true,
  "versaoTermo": "1.0"
}
```

A finalidade persistida é `PROGRAMA_FIDELIDADE` e a base legal registrada é `CONSENTIMENTO`. A revogação mantém o saldo e o histórico já existentes, mas impede novos créditos e resgates até uma nova concessão.

Resgate simples de pontos:

```http
POST /api/v1/fidelidade/{clienteId}/resgates
Authorization: Bearer token-jwt
Content-Type: application/json
```

```json
{
  "pontos": 10
}
```

O resgate utiliza bloqueio pessimista na carteira, registra uma movimentação `DEBITO` e retorna `409 SALDO_PONTOS_INSUFICIENTE` ou `409 CONSENTIMENTO_FIDELIDADE_NECESSARIO` quando a regra não é atendida.

## Testes automatizados

A suíte possui testes unitários e de integração para autenticação, autorização, cardápio, pedidos, checkout, cancelamento, fidelidade, consentimento, resgate, autorização por cliente, filtros, detalhes, ciclo de status, criptografia, fingerprints e auditoria.

Com Maven instalado:

```powershell
mvn test
```

### Execução dos testes pelo Docker no Windows

Crie uma base separada para os testes:

```powershell
docker compose exec -T postgres createdb -U raizes_app raizes_test
```

Se `raizes_test` já existir, não é necessário criá-la novamente.

Informe a senha utilizada no `.env`:

```powershell
$postgresPassword = Read-Host "Informe a senha do PostgreSQL"
```

Execute:

```powershell
docker run --rm `
  --env-file .env `
  --mount "type=bind,source=$($PWD.Path),target=/workspace" `
  --mount "type=volume,source=raizes-maven-cache,target=/root/.m2" `
  --workdir /workspace `
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/raizes_test `
  --env SPRING_DATASOURCE_USERNAME=raizes_app `
  --env SPRING_DATASOURCE_PASSWORD=$postgresPassword `
  --env SPRING_REDIS_HOST=host.docker.internal `
  --env SPRING_REDIS_PORT=6379 `
  --env SPRING_DATA_REDIS_DATABASE=15 `
  --env SPRING_RABBITMQ_HOST=host.docker.internal `
  --env SPRING_RABBITMQ_PORT=5672 `
  --env SPRING_RABBITMQ_USERNAME=guest `
  --env SPRING_RABBITMQ_PASSWORD=guest `
  maven:3.9.11-eclipse-temurin-17 `
  mvn -q test
$LASTEXITCODE
```

O resultado esperado é:

```text
0
```

Os relatórios são gerados em:

```text
target/surefire-reports/
```

Para visualizar o resumo no PowerShell:

```powershell
Get-ChildItem target\surefire-reports\*.txt |
    Select-String "Tests run:"
```

## Testes da API com Postman e Newman

A coleção executável está disponível em:

```text
docs/postman_collection.json
```

Ela contém 35 requisições e 78 verificações automatizadas, abrangendo autenticação, autorização, validações, cardápio, pedidos, checkout, cancelamento, pagamento, campanhas, fidelidade, consentimento, resgate e ciclo de status.

Com a aplicação em execução, a coleção pode ser testada no Windows sem instalar o Postman, utilizando o Newman pelo Docker:

```powershell
docker run --rm `
  --mount "type=bind,source=$($PWD.Path),target=/etc/newman" `
  postman/newman:alpine `
  run /etc/newman/docs/postman_collection.json `
  --env-var "baseUrl=http://host.docker.internal:8081"
$LASTEXITCODE
```

O resultado esperado é `0`, com 35 requisições e 78 verificações aprovadas.

## Integração contínua

O workflow `.github/workflows/ci.yaml` executa automaticamente:

1. Compilação com Java 17 e Maven.
2. Testes unitários e de integração.
3. Construção da imagem Docker.

O workflow é acionado em pull requests direcionadas à `main` e em pushes para `main` ou `develop`.

## Banco de dados

As migrações Flyway são executadas automaticamente durante a inicialização.

O banco inclui, entre outras, as seguintes estruturas:

- unidades;
- clientes;
- categorias e produtos;
- estoques por unidade;
- pedidos e itens;
- pagamentos;
- campanhas;
- carteiras, consentimentos e histórico de fidelidade;
- usuários;
- outbox;
- auditoria de segurança.

Para reiniciar todo o ambiente preservando os dados:

```powershell
docker compose down
docker compose up -d
```

Para apagar também o volume do PostgreSQL e recriar o banco desde a primeira migration:

```powershell
docker compose down -v
docker compose up -d --build
```

Atenção: `docker compose down -v` remove definitivamente os dados locais do banco.

## Segurança e LGPD

A implementação contém:

- autenticação stateless com JWT;
- autorização por perfil e por cliente nas rotas de fidelidade;
- BCrypt para senhas;
- AES-256-GCM com IV aleatório para CPF e e-mail;
- HMAC-SHA-256 para fingerprint do CPF;
- segredos externos ao repositório;
- respostas `401` e `403` padronizadas;
- auditoria de operações sensíveis;
- consentimento de fidelidade com finalidade, base legal, versão e timestamps de concessão/revogação;
- logs sem exposição direta de credenciais ou dados pessoais.

A rota demonstrativa `/api/v1/lgpd/anonimizar` ainda não executa anonimização persistente. Ela não deve ser apresentada como implementação completa do direito de eliminação.

## Limitações e evoluções futuras

- Implementar anonimização persistente para substituir a rota demonstrativa atual.
- Integrar efetivamente Redis ao cache do cardápio.
- Publicar eventos de domínio por RabbitMQ ou Kafka.
- Aplicar isolamento multi-tenant completo associado à identidade autenticada.
- Adicionar métricas de observabilidade e testes formais de carga.
- Evoluir relatórios gerenciais e operações administrativas.

## Encerramento do ambiente

```powershell
docker compose down
```

Repositório:

<https://github.com/samuelgarabini/raizes-nordeste-backend>
