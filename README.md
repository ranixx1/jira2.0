# Jira Service

Versão melhorada do meu Jira anterior. Gerencia chamados, comentários, times, categorias e subtópicos. Valida JWTs emitidos pelo **Authentication Service** para autenticar todas as requisições.

> **Porta padrão:** `8082`

---

## Tecnologias

- Java 17 + Spring Boot 3
- Spring Security OAuth2 Resource Server (validação de JWT)
- Spring Data JPA + Hibernate
- Lombok

---

## Estrutura do projeto

```
src/main/java/com/example/jira/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── ChamadoController.java        # /chamados/**
│   ├── TimeController.java           # /times/**
│   └── ConfigChamadoController.java  # /config/chamados/**
├── dto/
│   └── CategoriaResumoDTO.java
│   ├── ChamadoRequestDTO.java
│   ├── ChamadoResponseDTO.java
│   └── ComentarioDTO.java
│   └── ComentarioRequestDTO.java
│   └── SubtopicoRequestDTO.java
├── enums/
│   ├── Status.java
│   ├── Prioridade.java
│   └── Escopo.java
├── model/
│   ├── Chamado.java
│   ├── Comentario.java
│   ├── Time.java
│   ├── Categoria.java
│   └── Subtopico.java
├── repository/
│   ├── ChamadoRepository.java
│   ├── CategoriaRepository.java
│   ├── SubtopicoRepository.java
│   └── TimeRepository.java
└── service/
  └── ChamadoService.java
  └── ChamadoConfigService.java
```

---

## Configuração

```properties
# application.properties
spring.application.name=jira
spring.datasource.url=jdbc:mysql://localhost:3306/jira_db
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}

# Chave secreta JWT — mesma usada no Authentication Service
spring.security.oauth2.resourceserver.jwt.secret-key=${JWT_SECRET_BASE64}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

> **Atenção:** a propriedade `jwt.secret-key` deve conter a chave **em Base64**, idêntica à configurada no Auth Service.

---

## Enums

### `Status`
| Valor | Descrição |
|-------|-----------|
| `ABERTO` | Chamado recém-criado, aguardando atendimento |
| `EM_PROGRESSO` | Em atendimento por um agente |
| `AGUARDANDO_USUARIO` | Aguardando retorno ou informação do solicitante |
| `FECHADO` | Chamado encerrado |

### `Prioridade`
`BAIXA` · `NORMAL` · `ALTA`

### `Escopo`
| Valor | Descrição |
|-------|-----------|
| `TODOS` | Visível para todos os usuários com acesso |
| `SOMENTE_EU` | Visível apenas para o criador e admins |

---

## Endpoints

> Todos os endpoints exigem o header `Authorization: Bearer <token>`.

### Chamados — `/chamados`

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/chamados` | Cria um novo chamado |
| `GET` | `/chamados` | Lista todos os chamados |
| `GET` | `/chamados/id/{id}` | Busca chamado por ID |
| `GET` | `/chamados/status/{status}` | Filtra por status |
| `GET` | `/chamados/prioridade/{prioridade}` | Filtra por prioridade |
| `PUT` | `/chamados/{id}/status` | Altera o status do chamado |
| `POST` | `/chamados/{id}/comentarios` | Adiciona comentário |

#### `POST /chamados`
```json
{
  "categoriaId": 2,
  "subtopicoId": 5,
  "outroSubtopico": null,
  "prioridade": "ALTA",
  "titulo": "Sistema fora do ar",
  "descricao": "O sistema parou de responder após atualização.",
  "escopo": "TODOS"
}
```

#### `PUT /chamados/{id}/status`
```json
"EM_PROGRESSO"
```

#### `POST /chamados/{id}/comentarios`
```json
{
  "mensagem": "Verificado. O problema é no servidor de cache."
}
```

---

### Times — `/times`

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/times` | Lista todos os times |
| `GET` | `/times/{id}` | Busca time por ID |
| `POST` | `/times?nome=NomeDoTime` | Cria um novo time |
| `DELETE` | `/times/{id}` | Remove um time |
| `GET` | `/times/{id}/membros` | Lista IDs dos membros |
| `POST` | `/times/{id}/membros` | Adiciona membro ao time |
| `DELETE` | `/times/{id}/membros/{userId}` | Remove membro do time |

#### `POST /times/{id}/membros`
```json
{
  "userId": 42
}
```

---

### Configuração de Chamados — `/config/chamados`

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/config/chamados/disponiveis` | Lista categorias com subtópicos |
| `POST` | `/config/chamados/categorias?nome=X&timeId=1` | Cria categoria |
| `DELETE` | `/config/chamados/categorias/{id}` | Remove categoria e seus subtópicos |
| `POST` | `/config/chamados/subtopicos?nome=X&categoriaId=1` | Cria subtópico |
| `DELETE` | `/config/chamados/subtopicos/{id}` | Remove subtópico |

---

## Modelo de dados

```
Time (1) ──────── (N) Categoria (1) ──── (N) Subtopico
                        │
                        │ (via subtopico_id)
                   (N) Chamado (1) ───── (N) Comentario
```

- `Chamado` armazena apenas `userId` (Long) — o nome do usuário é buscado do token JWT.
- `Time` armazena apenas os IDs dos membros (`Set<Long>`) via `@ElementCollection`.
- `Comentario` não pode ser adicionado a chamados com status `FECHADO`.

---

## Como executar

```bash
git clone https://github.com/ranixx1/jira2.0.git
cd jira2.0

export DB_USER=root
export DB_PASS=sua_senha
export JWT_SECRET_BASE64=suaChaveEmBase64

./mvnw spring-boot:run
```

O serviço sobe em `http://localhost:8082`.

> O **Authentication Service** deve estar rodando antes, pois o Jira Service valida os tokens gerados por ele.

---

## Regras de negócio

- Um chamado é criado com status `ABERTO` automaticamente.
- Comentários em chamados `FECHADO` lançam `IllegalStateException`.
- O método `fechar()` na entidade `Chamado` impede fechamento duplo.
- `CascadeType.ALL` nos comentários: deletar um chamado remove todos os seus comentários.
- Deletar uma categoria remove todos os subtópicos vinculados (`orphanRemoval = true`).

---

## Roadmap planejado

- [x] DTO de resposta para todos os endpoints (evitar serialização de entidades)
- [ ] Endpoint `GET /chamados/meus` — filtra por `userId` do token
- [ ] Campo `responsavelId` no `Chamado` — atribuição de agente
- [ ] Histórico de status com auditoria por usuário
- [ ] Dashboard de métricas (`GET /chamados/metricas`)
- [ ] Filtros combinados por status + prioridade + categoria
- [ ] **Página de configurações do grupo** — menções recebidas e chamados resolvidos por time
- [ ] **Gestão de documentos** — upload e vínculo de documentações a chamados e categorias