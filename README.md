# Tessitura Jira

Sistema de gerenciamento de chamados inspirado no Jira Service Management.

A aplicação organiza os atendimentos através de uma estrutura hierárquica de **Portais**, **Categorias** e **Subtópicos**, permitindo que diferentes áreas da empresa possuam seus próprios catálogos de serviços.

Todas as requisições são autenticadas através de JWT emitido pelo **Authentication Service**.

> **Porta padrão:** `8082`

---

# Tecnologias

- Java 17 + Spring Boot 3
- Spring Security OAuth2 Resource Server (validação de JWT)
- Spring Data JPA + Hibernate
- MySQL
- Lombok

---

# Estrutura do projeto

```text
src/main/java/com/example/jira/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── ChamadoController.java
│   ├── TimeController.java
│   └── ConfigChamadoController.java
├── dto/
│   ├── CategoriaResumoDTO.java
│   ├── ChamadoRequestDTO.java
│   ├── ChamadoResponseDTO.java
│   ├── ComentarioDTO.java
│   ├── ComentarioRequestDTO.java
│   └── SubtopicoRequestDTO.java
├── enums/
│   ├── Escopo.java
│   ├── Prioridade.java
│   └── Status.java
├── model/
│   ├── Portal.java
│   ├── Categoria.java
│   ├── Subtopico.java
│   ├── Chamado.java
│   ├── Comentario.java
│   └── Time.java
├── repository/
│   ├── PortalRepository.java
│   ├── CategoriaRepository.java
│   ├── SubtopicoRepository.java
│   ├── ChamadoRepository.java
│   └── TimeRepository.java
└── service/
    ├── ChamadoService.java
    └── ChamadoConfigService.java
```

---

# Configuração

```properties
spring.application.name=jira

spring.datasource.url=jdbc:mysql://localhost:3306/jira_db
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}

spring.security.oauth2.resourceserver.jwt.secret-key=${JWT_SECRET_BASE64}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

> **Importante:** a chave JWT deve ser exatamente a mesma utilizada pelo Authentication Service.

---

# Organização dos chamados

Os chamados seguem uma estrutura hierárquica.

```text
Portal
 ├── Categoria
 │      ├── Subtópico
 │      ├── Subtópico
 │      └── Subtópico
 │
 ├── Categoria
 │      └── Subtópico
 │
 └── Categoria
```

Ao abrir um chamado o usuário seleciona:

- Portal
- Categoria
- Subtópico

Essa arquitetura facilita a organização de serviços por departamento, área de negócio ou produto.

---

# Enums

## Status

| Valor | Descrição |
|--------|-----------|
| ABERTO | Chamado recém-criado |
| EM_PROGRESSO | Chamado em atendimento |
| AGUARDANDO_USUARIO | Aguardando resposta do solicitante |
| FECHADO | Chamado encerrado |

## Prioridade

- BAIXA
- NORMAL
- ALTA

## Escopo

| Valor | Descrição |
|--------|-----------|
| TODOS | Visível para todos os usuários autorizados |
| SOMENTE_EU | Visível apenas para o criador |

---

# Endpoints

Todos os endpoints exigem:

```
Authorization: Bearer <token>
```

---

# Chamados

Base:

```
/chamados
```

| Método | Endpoint | Descrição |
|---------|----------|-----------|
| POST | `/chamados` | Cria um chamado |
| GET | `/chamados` | Lista chamados |
| GET | `/chamados/id/{id}` | Busca chamado por ID |
| GET | `/chamados/status/{status}` | Filtra por status |
| GET | `/chamados/prioridade/{prioridade}` | Filtra por prioridade |
| PUT | `/chamados/{id}/status` | Atualiza status |
| POST | `/chamados/{id}/comentarios` | Adiciona comentário |

## Exemplo

```json
{
  "portalId": 1,
  "categoriaId": 3,
  "subtopicoId": 8,
  "outroSubtopico": null,
  "prioridade": "ALTA",
  "titulo": "Notebook não liga",
  "descricao": "Após atualização o equipamento não inicializa.",
  "escopo": "TODOS"
}
```

### Atualizar status

```json
"EM_PROGRESSO"
```

### Adicionar comentário

```json
{
  "mensagem": "Problema identificado e em análise."
}
```

---

# Times

Base:

```
/times
```

| Método | Endpoint | Descrição |
|---------|----------|-----------|
| GET | `/times` | Lista times |
| GET | `/times/{id}` | Busca um time |
| POST | `/times?nome=Suporte` | Cria um time |
| DELETE | `/times/{id}` | Remove um time |
| GET | `/times/{id}/membros` | Lista membros |
| POST | `/times/{id}/membros` | Adiciona membro |
| DELETE | `/times/{id}/membros/{userId}` | Remove membro |

### Adicionar membro

```json
{
  "userId": 42
}
```

---

# Configuração dos chamados

Base:

```
/config/chamados
```

## Portais

| Método | Endpoint | Descrição |
|---------|----------|-----------|
| GET | `/portais` | Lista portais |
| POST | `/portais` | Cria um portal |
| DELETE | `/portais/{id}` | Remove um portal |

### Criar portal

```json
{
  "nome": "Tecnologia",
  "descricao": "Portal destinado às solicitações de TI."
}
```

---

## Categorias

| Método | Endpoint | Descrição |
|---------|----------|-----------|
| GET | `/categorias` | Lista categorias |
| POST | `/categorias` | Cria categoria |
| DELETE | `/categorias/{id}` | Remove categoria |

### Criar categoria

```json
{
  "nome": "Hardware",
  "portalId": 1
}
```

---

## Subtópicos

| Método | Endpoint | Descrição |
|---------|----------|-----------|
| GET | `/subtopicos` | Lista subtópicos |
| POST | `/subtopicos` | Cria subtópico |
| DELETE | `/subtopicos/{id}` | Remove subtópico |

### Criar subtópico

```json
{
  "nome": "Notebook",
  "categoriaId": 3
}
```

---

# Modelo de dados

```text
Portal (1)
   │
   └────────────── (N) Categoria
                        │
                        └────────────── (N) Subtopico
                                              │
                                              │
                                   (N) Chamado (1)
                                              │
                                              │
                                       (N) Comentario

Time
 └── membros
```

---

# Regras de negócio

- Um Portal pode possuir várias Categorias.
- Uma Categoria pertence obrigatoriamente a um Portal.
- Uma Categoria pode possuir vários Subtópicos.
- Um Subtópico pertence obrigatoriamente a uma Categoria.
- Um Chamado referencia Portal, Categoria e Subtópico.
- Chamados são criados automaticamente com status **ABERTO**.
- Comentários não podem ser adicionados em chamados **FECHADO**.
- O método `fechar()` impede fechamento duplicado.
- Deletar um Portal remove todas as Categorias e Subtópicos associados.
- Deletar uma Categoria remove todos os seus Subtópicos.
- O usuário autenticado é obtido através do JWT; apenas o `userId` é armazenado no chamado.

---

# Como executar

```bash
git clone https://github.com/ranixx1/tessitura_jira.git

cd tessitura

export DB_USER=root
export DB_PASS=sua_senha
export JWT_SECRET_BASE64=sua_chave_base64

./mvnw spring-boot:run
```

O serviço estará disponível em:

```
http://localhost:8082
```

> O Authentication Service deve estar em execução para validação dos JWTs.

---

# Roadmap

## Chamados

- [x] CRUD de chamados
- [x] Comentários
- [x] Portais
- [x] Categorias
- [x] Subtópicos
- [ ] Upload de anexos
- [ ] Campos personalizados
- [ ] Histórico de alterações
- [ ] SLA por chamado

## Atendimento

- [x] Responsável pelo chamado
- [x] Atribuição automática por regras
- [x] Observadores
- [x] Menções (@usuário)
- [x] Comentários internos

## Administração

- [x] Permissões por Portal
- [x] Formulários personalizados por Portal
- [ ] Base de Conhecimento (Knowledge Base)
- [ ] Artigos vinculados às Categorias

## Relatórios

- [ ] Dashboard
- [ ] Chamados por Portal
- [ ] Chamados por Categoria
- [ ] Chamados por Prioridade
- [ ] Tempo médio de atendimento
- [ ] Cumprimento de SLA
