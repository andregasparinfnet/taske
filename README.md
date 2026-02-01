# 🚀 LifeOS - Sistema de Gestão Pessoal

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0--beta.1-orange.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Tests](https://img.shields.io/badge/tests-44%20passing-brightgreen.svg)
![Coverage](https://img.shields.io/badge/coverage-98%25-brightgreen.svg)

**Uma aplicação full-stack moderna para gerenciamento de compromissos pessoais e profissionais**

[Funcionalidades](#-funcionalidades) •
[Tecnologias](#-tecnologias) •
[Instalação](#-instalação) •
[Uso](#-uso) •
[Testes](#-testes)

</div>

---

## 📋 Sobre o Projeto

O **LifeOS** é um sistema completo de gestão pessoal que permite organizar compromissos, tarefas e atividades do dia a dia. Com uma interface moderna e intuitiva, oferece múltiplas visualizações (Dashboard, Agenda e Kanban) para atender diferentes preferências de organização.

### 🎯 Motivação

Desenvolvido como projeto acadêmico para demonstrar competências em desenvolvimento full-stack, incluindo:
- Arquitetura RESTful
- Autenticação JWT
- Testes automatizados
- Design responsivo moderno

---

## ✨ Funcionalidades

### 🔐 Autenticação e Segurança
- ✅ Login e registro de usuários
- ✅ Autenticação via JWT (JSON Web Token)
- ✅ Isolamento de dados por usuário (multi-tenant)
- ✅ Proteção de rotas e endpoints

### 📊 Dashboard
- ✅ Visão geral com estatísticas em tempo real
- ✅ Contadores: Total, Perícias, Próximos compromissos
- ✅ Lista de compromissos recentes
- ✅ Formulário rápido para novos itens

### 📅 Agenda
- ✅ Visualização cronológica por data
- ✅ Agrupamento inteligente (Hoje, dias da semana)
- ✅ Formatação brasileira de datas e horários
- ✅ Indicadores visuais por tipo

### 🗂️ Quadro Kanban
- ✅ Três colunas: A Fazer, Em Progresso, Concluído
- ✅ Drag and drop para mover cards
- ✅ Atualização otimista da interface
- ✅ Sincronização automática com o backend

### 📝 Gestão de Compromissos
- ✅ CRUD completo (Criar, Ler, Atualizar, Deletar)
- ✅ Tipos: Perícia, Trabalho, Família, Financeiro
- ✅ Status: Pendente, Em Andamento, Concluído
- ✅ Campos: Título, Data/Hora, Valor, Descrição

---

## 🛠️ Tecnologias

### Backend
| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| Java | 17+ | Linguagem principal |
| Spring Boot | 3.x | Framework web |
| Spring Security | 6.x | Autenticação e autorização |
| PostgreSQL | 15+ | Banco de dados |
| JWT | - | Tokens de autenticação |
| Maven | 3.x | Gerenciador de dependências |

### Frontend
| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| React | 18.x | Biblioteca UI |
| Vite | 5.x | Build tool |
| Axios | 1.x | Cliente HTTP |
| @hello-pangea/dnd | 16.x | Drag and drop |
| Lucide React | 0.x | Ícones |
| Vitest | 2.x | Framework de testes |

### DevOps
| Tecnologia | Descrição |
|------------|-----------|
| Docker | Containerização |
| Docker Compose | Orquestração |
| Git | Versionamento |

---

## 🔒 Segurança

O LifeOS implementa múltiplas camadas de proteção para garantir a segurança dos dados e prevenir vulnerabilidades comuns:

### ✅ Proteções Implementadas

| Proteção | Status | Descrição |
|----------|--------|-----------|
| **CSRF Protection** | ✅ Ativo | Double Submit Cookie Pattern |
| **Session Fixation Prevention** | ✅ Ativo | Session ID regeneration após login |
| **Timing Attack Protection** | ✅ Ativo | Mensagens genéricas + BCrypt constant-time |
| **Rate Limiting** | ✅ Ativo | 5 requisições/minuto por IP (login) |
| **JWT Security** | ✅ Ativo | Access Token + Refresh Token |
| **CORS Protection** | ✅ Ativo | Whitelist configurável |
| **Security Headers** | ✅ Ativo | CSP, HSTS, X-Frame-Options |
| **Password Hashing** | ✅ Ativo | BCrypt (strength 12) |

### 🛡️ Auditoria de Vulnerabilidades

**Status:** 🟢 **0 vulnerabilidades conhecidas**

```bash
# Backend
./mvnw clean compile
[INFO] BUILD SUCCESS ✅

# Frontend
npm audit
found 0 vulnerabilities ✅
```

### 📊 Testes de Segurança

**11 testes E2E** cobrindo:
- ✅ CSRF Token generation & validation (6 testes)
- ✅ Session Fixation Prevention (2 testes)
- ✅ Timing Attack Protection (3 testes)

**Cobertura:** 100% das vulnerabilidades críticas

### 📄 Documentação de Segurança

- [Security Implementation Status](docs/SECURITY-IMPLEMENTATION-STATUS.md) - Status detalhado das implementações
- [Security Testing Report](docs/SECURITY-TESTING-REPORT.md) - Relatório completo de testes E2E
- [Security Plan](docs/PLAN.md) - Plano estratégico de segurança

### 🔐 Boas Práticas

**Backend:**
- Autenticação stateless via JWT
- Senhas hash com BCrypt (never plaintext)
- Validação de entrada em todos os endpoints
- Isolamento de dados por usuário (multi-tenant)

**Frontend:**
- Access Token armazenado em memória (não em localStorage)
- Refresh Token via httpOnly cookie
- CSRF Token enviado automaticamente (Double Submit Cookie)
- Timeout automático de sessão

---

## 🚀 Instalação


### Pré-requisitos
- Java 17+
- Node.js 18+
- PostgreSQL 15+ (ou Docker)
- Maven 3+

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/lifeos.git
cd lifeos
```

### 2. Configure o banco de dados

**Opção A: Docker (Recomendado)**
```bash
docker-compose up -d postgres
```

**Opção B: PostgreSQL local**
```sql
CREATE DATABASE taske;
CREATE USER perit WITH PASSWORD 'sua-senha';
GRANT ALL PRIVILEGES ON DATABASE taske TO perit;
```

### 3. Inicie o Backend
```bash
cd backend
./mvnw spring-boot:run
```
O servidor estará disponível em `http://localhost:8080`

### 4. Inicie o Frontend
```bash
cd frontend
npm install
npm run dev
```
A aplicação estará disponível em `http://localhost:5173`

---

## 📖 Uso

### Primeiro Acesso
1. Acesse `http://localhost:5173`
2. Clique em "Criar conta"
3. Preencha usuário e senha
4. Faça login com suas credenciais

### Navegação
- **Dashboard**: Visão geral e formulário rápido
- **Agenda**: Lista cronológica de compromissos
- **Quadros**: Kanban com drag and drop

### API Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/register` | Registrar usuário |
| POST | `/api/auth/login` | Fazer login |
| GET | `/api/compromissos` | Listar compromissos |
| POST | `/api/compromissos` | Criar compromisso |
| PUT | `/api/compromissos/{id}` | Atualizar compromisso |
| DELETE | `/api/compromissos/{id}` | Deletar compromisso |

---

## ✅ Testes

### Frontend
```bash
cd frontend

# Rodar testes
npm test

# Rodar testes com cobertura
npm test -- --coverage --run
```

### Cobertura Atual

| Arquivo | Statements | Branches | Functions | Lines |
|---------|------------|----------|-----------|-------|
| **Total** | **98.01%** | **92.38%** | **86.20%** | **98.01%** |
| App.jsx | 94.52% | 81.25% | 80.00% | 94.52% |
| KanbanView.jsx | 93.16% | 87.50% | 83.33% | 93.16% |
| Login.jsx | 98.42% | 94.11% | 100% | 98.42% |
| DashboardView.jsx | 100% | 100% | 100% | 100% |
| AgendaView.jsx | 100% | 91.66% | 100% | 100% |

---

## 📁 Estrutura do Projeto

```
lifeos/
├── backend/
│   ├── src/main/java/com/example/backend/
│   │   ├── controller/     # Controllers REST
│   │   ├── model/          # Entidades JPA
│   │   ├── repository/     # Repositórios
│   │   ├── security/       # Configuração JWT
│   │   └── service/        # Lógica de negócio
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── App.jsx         # Componente principal
│   │   ├── Login.jsx       # Tela de login/registro
│   │   ├── DashboardView.jsx
│   │   ├── AgendaView.jsx
│   │   ├── KanbanView.jsx
│   │   └── *.test.jsx      # Testes unitários
│   └── package.json
│
├── docker-compose.yml
└── README.md
```

---

## 📜 Histórico de Versões

### v1.0.0 (2026-01-31) - Lançamento Inicial
**Etapa 0 - Fundação**
- ✅ Estrutura base do projeto (Spring Boot + React)
- ✅ CRUD completo de compromissos
- ✅ Interface com Dashboard, Agenda e Kanban
- ✅ Integração com PostgreSQL

**Etapa 1 - Segurança**
- ✅ Autenticação JWT implementada
- ✅ Registro e login de usuários
- ✅ Isolamento de dados por usuário
- ✅ Proteção de rotas no frontend

**Etapa 2 - Qualidade**
- ✅ 44 testes unitários no frontend
- ✅ Cobertura de código de 98%
- ✅ Testes de drag-and-drop (Kanban)
- ✅ Testes de autenticação e CRUD

---

## 👤 Autor

**Seu Nome**
- GitHub: [@seu-usuario](https://github.com/seu-usuario)
- LinkedIn: [Seu Perfil](https://linkedin.com/in/seu-perfil)

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

<div align="center">

**⭐ Se este projeto foi útil, considere dar uma estrela!**

</div>
