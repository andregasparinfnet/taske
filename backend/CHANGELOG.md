# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [1.0.0-rc.1] - 2026-02-01

### 🚀 Release Candidate

Versão candidata a lançamento com backend sincronizado e melhorias de segurança.

### 🔧 Alterado
- Sincronização de versão com o Frontend.
- Preparação para lançamento Release Candidate.

## [1.0.0-beta.1] - 2026-01-31

### 🚧 Versão Beta

Primeira versão estável do LifeOS Backend - API REST para sistema de gestão pessoal.

### ✨ Adicionado

#### Autenticação e Segurança
- Implementação completa de autenticação JWT (JSON Web Token)
- Endpoint de registro de usuários (`POST /api/auth/register`)
- Endpoint de login com geração de token (`POST /api/auth/login`)
- Configuração do Spring Security com filtros JWT
- Isolamento de dados por usuário (multi-tenant)
- Proteção de todos os endpoints da API com autenticação Bearer

#### API REST de Compromissos
- CRUD completo de compromissos (`/api/compromissos`)
  - `GET` - Listar compromissos do usuário autenticado
  - `POST` - Criar novo compromisso
  - `PUT /{id}` - Atualizar compromisso existente
  - `DELETE /{id}` - Remover compromisso
- Validação de dados com Bean Validation
- Tratamento de exceções com mensagens amigáveis

#### Modelo de Dados
- Entidade `Usuario` com campos: id, username, password (criptografado)
- Entidade `Compromisso` com campos:
  - id, titulo, descricao, dataHora, valor
  - tipo: PERICIA, TRABALHO, FAMILIA, FINANCEIRO
  - status: PENDENTE, EM_ANDAMENTO, CONCLUIDO
  - Relacionamento ManyToOne com Usuario

#### Infraestrutura
- Configuração do Spring Boot 3.4.1
- Integração com PostgreSQL via Spring Data JPA
- Suporte a CORS para integração com frontend
- DevTools para desenvolvimento com hot-reload
- Lombok para redução de boilerplate

#### Qualidade de Código
- Configuração do JaCoCo para cobertura de testes
- Testes unitários com Spring Security Test
- Estrutura de pacotes organizada (controller, model, repository, security, service)

### 🔧 Configuração

#### Variáveis de Ambiente
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taske
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

### 📦 Dependências Principais
- Spring Boot Starter Web 3.4.1
- Spring Boot Starter Data JPA 3.4.1
- Spring Boot Starter Security 3.4.1
- Spring Boot Starter Validation 3.4.1
- PostgreSQL Driver
- JWT (jjwt-api, jjwt-impl, jjwt-jackson) 0.11.5
- Lombok
- JaCoCo 0.8.11

---

## [Unreleased]

### Planejado
- [ ] Recuperação de senha via email
- [ ] Refresh token para renovação de sessão
- [ ] Documentação Swagger/OpenAPI
- [ ] Rate limiting para proteção contra ataques
- [ ] Cache com Redis
- [ ] Containerização com Docker

---

[1.0.0-rc.1]: https://github.com/seu-usuario/lifeos/releases/tag/v1.0.0-rc.1-backend
[1.0.0-beta.1]: https://github.com/seu-usuario/lifeos/releases/tag/v1.0.0-beta.1-backend
[Unreleased]: https://github.com/seu-usuario/lifeos/compare/v1.0.0-rc.1-backend...HEAD
