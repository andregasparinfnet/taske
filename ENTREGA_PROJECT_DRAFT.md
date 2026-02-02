# PROJETO FINAL - ENTREGA

**Nome do Aluno:** [SEU_NOME_AQUI]
**Disciplina:** Projeto de Bloco - Desenvolvimento Web
**Projeto:** LifeOS (Taske)

---

## 🔗 Link do Repositório (Código Fonte)

[**https://github.com/andregasparinfnet/taske**](https://github.com/andregasparinfnet/taske)

O repositório contém:
- **Backend**: API REST em Java com Spring Boot.
- **Frontend**: Aplicação React (SPA).
- **Docker**: Arquivos de configuração de conteinerização.
- **Documentação**: Guias de instalação, testes e arquitetura.

---

## 📋 Visão Geral do Projeto

O **LifeOS** é um sistema de gestão pessoal focado em produtividade e organização. Ele permite gerenciar compromissos, tarefas e status de forma visual e intuitiva. O projeto foi construído utilizando as melhores práticas de desenvolvimento moderno, incluindo arquitetura segura, tratamento de erros robusto e interface responsiva.

### Tecnologias Utilizadas

*   **Backend:** Java 21, Spring Boot 3, Spring Security (JWT), PostgreSQL.
*   **Frontend:** React (Vite), CSS Moderno, Axios (Interceptadores).
*   **DevOps:** Docker, Docker Compose, Render (Deploy), GitHub Actions (CI).

### Principais Desafios

*   Implementação de um sistema de autenticação seguro (JWT + Refresh Token + Cookies HttpOnly).
*   Gerenciamento de estado otimizado no Frontend para evitar renderizações desnecessárias.
*   Configuração do ambiente de produção (Render) com banco de dados PostgreSQL.

---

## 🚀 Como Rodar a Aplicação

### 1. Pré-requisitos
*   Docker e Docker Compose instalados.
*   Git instalado.

### 2. Rodar com Docker (Recomendado)

O projeto está totalmente conteinerizado. Para rodar todo o ambiente (Banco + Backend + Frontend):

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/andregasparinfnet/taske.git
    cd taske
    ```

2.  **Inicie os serviços:**
    ```bash
    docker-compose up --build
    ```

3.  **Acesse a aplicação:**
    *   **Frontend:** `http://localhost:5173`
    *   **Backend API:** `http://localhost:8080/api`

### 3. Deploy (Produção)

A aplicação está configurada para deploy automático no **Render** via Blueprint (Infraestrutura como Código).

*   **Link da Aplicação:** [INSIRA_O_LINK_DO_SEU_DEPLOY_AQUI] (Ex: `https://taske-frontend.onrender.com`)
*   **Método:** O arquivo `render.yaml` na raiz do projeto orquestra automaticamente o Build e Deploy do Frontend, Backend e Banco de Dados.

---

## 📍 Mapeamento de Funcionalidades
Abaixo estão os links diretos para o código fonte responsável por cada funcionalidade solicitada.

### 1. Login e Autenticação
*   **Backend (Controller):** [`AuthController.java`](https://github.com/andregasparinfnet/taske/blob/main/backend/src/main/java/com/example/backend/controller/AuthController.java)
*   **Frontend (Tela):** [`Login.jsx`](https://github.com/andregasparinfnet/taske/blob/main/frontend/src/views/Login/Login.jsx)
*   **Serviço de API (Interceptadores):** [`api.js`](https://github.com/andregasparinfnet/taske/blob/main/frontend/src/services/api.js)

### 2. Gestão de Compromissos (CRUD)
*   **Listagem (Dashboard):** [`Dashboard.jsx`](https://github.com/andregasparinfnet/taske/blob/main/frontend/src/views/Dashboard/Dashboard.jsx)
*   **Criação/Edição:** [`CompromissoForm.jsx`](https://github.com/andregasparinfnet/taske/blob/main/frontend/src/components/CompromissoForm/CompromissoForm.jsx)
*   **Backend (Controller):** [`CompromissoController.java`](https://github.com/andregasparinfnet/taske/blob/main/backend/src/main/java/com/example/backend/controller/CompromissoController.java)

### 3. Segurança 
*   **Configuração JWT:** [`SecurityConfig.java`](https://github.com/andregasparinfnet/taske/blob/main/backend/src/main/java/com/example/backend/config/SecurityConfig.java)

---

## 📷 Demonstrativo de Funcionamento (Prints)

> **Instrução:** Para gerar o PDF final, substitua os espaços abaixo pelos prints da sua aplicação rodando.

### Tela de Login
*(Cole aqui o print da tela de login)*

### Dashboard (Listagem de Tarefas)
*(Cole aqui o print do Dashboard com tarefas cadastradas)*

### Criação de Nova Tarefa
*(Cole aqui o print do modal ou tela de cadastro)*

### Responsividade (Mobile)
*(Cole aqui um print da tela em modo mobile)*

---

## ✅ Testes Realizados

Os testes automatizados cobrem as camadas críticas da aplicação.

### Como executar os testes:

**Backend (JUnit):**
```bash
cd backend
mvn test
```

**Frontend (Vitest/Playwright):**
```bash
cd frontend
npm test
```
