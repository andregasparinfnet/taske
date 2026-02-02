# Documentação do Projeto: LifeOS - Sistema de Gestão Pessoal

Este documento detalha as diretrizes técnicas, arquitetura e instruções de operação do projeto **LifeOS**, desenvolvido como um sistema full-stack moderno para gerenciamento de compromissos e tarefas.

---

## 1. Visão Geral do Projeto

### Objetivo
O **LifeOS** foi concebido com o propósito de oferecer uma solução centralizada e segura para a organização de rotinas pessoais e profissionais. O sistema permite que o usuário gerencie compromissos (Perícias, Trabalhos, Finanças, etc.) através de múltiplas perspectivas: um Dashboard analítico, uma Agenda cronológica e um Quadro Kanban interativo.

O foco principal do desenvolvimento foi a **Segurança da Informação** e a **Qualidade de Código**, garantindo que os dados de cada usuário sejam isolados e protegidos contra vulnerabilidades comuns da web.

### Tecnologias Utilizadas
A stack tecnológica foi selecionada para garantir performance, escalabilidade e facilidade de teste:

*   **Backend:**
    *   **Java 21:** Versão estável mais recente do JDK para aproveitar as melhorias de performance.
    *   **Spring Boot 3.4.1:** Framework base para a criação da API RESTful.
    *   **Spring Security & JWT:** Implementação de autenticação stateless com Access e Refresh Tokens.
    *   **PostgreSQL:** Banco de dados relacional robusto para persistência de dados.
    *   **Maven:** Gerenciador de dependências e automação de builds.
    *   **Lombok:** Redução de código boilerplate.

*   **Frontend:**
    *   **React 19:** Biblioteca principal para construção da interface declarativa.
    *   **Vite:** Ferramenta de build e servidor de desenvolvimento ultra-rápido.
    *   **Axios:** Cliente HTTP para comunicação com a API, configurado com interceptores para segurança.
    *   **Lucide React:** Conjunto de ícones vetoriais modernos.
    *   **@hello-pangea/dnd:** Biblioteca para implementação do Drag and Drop no Kanban.

*   **DevOps & Qualidade:**
    *   **Docker & Docker Compose:** Containerização de toda a stack.
    *   **Vitest & React Testing Library:** Suite de testes para o frontend (Unitários e Integração).
    *   **JUnit 5 & Mockito:** Suite de testes para o backend.
    *   **Playwright:** Testes End-to-End (E2E) simulando navegação real do usuário.

### Principais Desafios Enfrentados
1.  **Segurança Avançada:** A implementação do padrão *Double Submit Cookie* para proteção contra CSRF, em conjunto com autenticação JWT, exigiu uma configuração refinada entre frontend e backend para garantir que os tokens fossem trocados de forma segura e transparente.
2.  **Sincronização de Estado (Kanban):** Garantir que a interface do Quadro Kanban respondesse instantaneamente ao usuário (*Optimistic UI*) enquanto sincronizava o novo estado com o banco de dados, tratando possíveis erros de rede e revertendo o estado se necessário.
3.  **Refatoração Arquitetural:** Transicionar de uma estrutura de arquivos plana para uma arquitetura orientada a domínios (`views`, `components`, `services`) no frontend, visando a escalabilidade do projeto.
4.  **Cobertura de Testes Elevada:** Manter uma cobertura superior a 90% em ambas as pontas, garantindo que refatorações não introduzissem regressões em fluxos críticos como login e edição de compromissos.

---

## 2. Passos para Rodar a Aplicação Localmente

### Pré-requisitos
*   **Java JDK 21** ou superior.
*   **Node.js 18** ou superior.
*   **PostgreSQL 15** ou superior (instalado e rodando).
*   **Maven 3.x**.

### Configuração do Banco de Dados
1.  Crie um banco de dados chamado `lifeos_db` (padrão) ou `taske` (se preferir, ajuste o application.properties).
2.  Configure as credenciais no arquivo `backend/src/main/resources/application.properties` ou via variáveis de ambiente.

### Executando o Backend
1.  Navegue até a pasta do backend:
    ```bash
    cd backend
    ```
2.  Execute o comando para iniciar a aplicação:
    ```bash
    ./mvnw spring-boot:run
    ```
*O servidor iniciará em `http://localhost:8080`.*

### Executando o Frontend
1.  Navegue até a pasta do frontend:
    ```bash
    cd frontend
    ```
2.  Instale as dependências:
    ```bash
    npm install
    ```
3.  Inicie o servidor de desenvolvimento:
    ```bash
    npm run dev
    ```
*A aplicação estará acessível em `http://localhost:5173`.*

---

## 3. Instruções de Deploy

### Produção com Docker (Recomendado)
O projeto está preparado para rodar em containers, o que facilita o deploy em qualquer provedor de nuvem (AWS, Azure, DigitalOcean).

1.  Certifique-se de que o Docker e o Docker Compose estão instalados.
2.  Na raiz do projeto, execute:
    ```bash
    docker-compose up -d --build
    ```
3.  O Docker Compose irá:
    *   Subir uma instância do PostgreSQL.
    *   Fazer o build e subir a API Backend.
    *   Fazer o build e subir o Frontend em um servidor otimizado.

### Variáveis de Ambiente Críticas
Para ambientes de produção, as seguintes variáveis devem ser configuradas (no arquivo `.env` ou no ambiente do servidor):

**Banco de Dados (Docker):**
*   `POSTGRES_DB`: Nome do banco (ex: lifeos_db).
*   `POSTGRES_USER`: Usuário do banco.
*   `POSTGRES_PASSWORD`: Senha do banco.

**Backend (Spring Boot):**
*   `SPRING_DATASOURCE_URL`: URL de conexão JDBC.
*   `SPRING_DATASOURCE_USERNAME`: Usuário do banco.
*   `SPRING_DATASOURCE_PASSWORD`: Senha do banco.
*   `JWT_SECRET`: Chave secreta longa e segura para assinatura dos tokens (mínimo 64 caracteres).
*   `CORS_ALLOWED_ORIGINS`: URLs permitidas para acessar a API (ex: https://meusite.com).

---

## 4. Garantia de Qualidade e Testes

O projeto segue a pirâmide de testes para garantir robustez em todos os níveis.

### Testes Automatizados no Frontend
*   **Unitários e Integração (Vitest):** Testam componentes React e lógica de negócio.
    ```bash
    cd frontend
    npm test
    ```
*   **E2E (Playwright):** Testam fluxos completos (Login -> Criação de Compromisso -> Exclusão).
    ```bash
    npx playwright test
    ```

### Testes Automatizados no Backend
*   **Unitários (JUnit 5):** Testam serviços e lógica isolada.
*   **Integração:** Testam a integração com o banco de dados e filtros de segurança.
*   **Performance:** Validam se os endpoints críticos respondem em menos de 200ms sob carga.
    ```bash
    cd backend
    ./mvnw test
    ```

### Relatórios de Cobertura
Atualmente, o projeto mantém:
*   **Frontend:** >90% de cobertura geral.
*   **Backend:** Configurado com JaCoCo para auditoria contínua de cobertura em cada build.

---
**Autor:** LifeOS Team
**Versão:** 1.0.0-rc.1
**Data:** 01 de Fevereiro de 2026
