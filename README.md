# Rede Raízes do Nordeste - Back-End API

Plataforma de back-end distribuída, escalável e multi-tenant para gerenciamento de checkout e pedidos multicanal da rede de franquias **Raízes do Nordeste**.

---

## 🚀 Tecnologias Utilizadas

- **Java 17 LTS** e **Spring Boot 3**
- **PostgreSQL 15** com **Row-Level Security (RLS)** para isolamento multi-tenant
- **Redis** para cache distribuído
- **RabbitMQ** para mensageria assíncrona (Integração com KDS)
- **Flyway** para migrações automatizadas do banco de dados
- **Docker** e **Docker Compose** para orquestração de contêineres
- **OpenAPI / Swagger** para documentação de rotas
- **GitHub Actions** para automação de CI/CD

---

## 🛠️ Como Executar o Projeto Localmente

### Pré-requisitos
- Docker Desktop e Docker Compose instalados
- Git instalado

### Passo a Passo

1. **Clonar o repositório:**
   ```bash
   git clone [https://github.com/samuelgarabini/raizes-nordeste-backend.git](https://github.com/samuelgarabini/raizes-nordeste-backend.git)
   cd raizes-nordeste-backend

2. Configurar as Variáveis de Ambiente:
    Copie o modelo .env.example para criar o seu arquivo de configurações locais .env:
    Bash

    cp .env.example .env

    (Ou crie o arquivo .env manualmente na raiz copiando os valores do .env.example)

3. Subir os Contêineres via Docker Compose:
    No terminal, dentro da pasta raiz do projeto, execute o comando:
    Bash

    docker-compose up -d --build

    Este comando irá provisionar as instâncias de PostgreSQL, Redis e RabbitMQ, compilar o código Java e executar automaticamente as migrações SQL pelo Flyway.

4. Acessar a Documentação Interativa (Swagger):
    Com a aplicação em execução, acesse a URL no seu navegador para visualizar e testar os endpoints:
    http://localhost:8080/swagger-ui/index.html


🧪 Validação e Testes com Postman

Para executar os testes das rotas da API:

    1. Abra o aplicativo Postman.

    2. Clique na opção Import e selecione o arquivo docs/postman_collection.json localizado no projeto.

    3. Execute as requisições importadas para validar os cenários de teste automatizados (autenticação JWT, criação de pedidos multicanal, simulação de pagamento mock, isolamento RLS e conformidade com LGPD).