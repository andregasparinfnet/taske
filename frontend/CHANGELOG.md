# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [1.0.0-rc.1] - 2026-02-01

### 🚀 Release Candidate

Versão candidata a lançamento com estrutura de frontend refatorada e testes validados.

### 🔧 Alterado
- Refatoração da estrutura de pastas do frontend (`views`, `components`, `services`).
- Centralização da lógica de API em `src/services/api.js`.
- Atualização de todos os caminhos de importação e mocks de teste.
- Sincronização de versões entre Frontend e Backend.

## [1.0.0-beta.1] - 2026-01-31

### 🚧 Versão Beta

Primeira versão estável do LifeOS Frontend - Interface React para sistema de gestão pessoal.

### ✨ Adicionado

#### Interface de Autenticação
- Tela de login com validação de campos
- Tela de registro de novos usuários
- Alternância fluida entre login e registro
- Armazenamento seguro de token JWT em memória
- Refresh Token via httpOnly cookie
- Logout com limpeza de sessão

#### Dashboard
- Visão geral com cards de estatísticas (KPIs)
  - Total de compromissos
  - Perícias agendadas
  - Próximos compromissos
- Widget de boas-vindas com contagem dinâmica
- Lista de compromissos recentes com ações rápidas
- Formulário simplificado para criação rápida

#### Agenda
- Visualização cronológica de compromissos
- Agrupamento inteligente por data
- Destaque para compromissos de "Hoje"
- Formatação brasileira de datas e horários
- Indicadores visuais por tipo de compromisso
- Exibição de valores quando aplicável

#### Quadro Kanban
- Três colunas: A Fazer, Em Progresso, Concluído
- Drag and drop com @hello-pangea/dnd
- Atualização otimista da interface
- Sincronização automática com backend via API
- Cards com informações resumidas
- Contadores por coluna

#### Componentes e Design
- Design system moderno com CSS customizado
- Paleta de cores profissional
- Ícones Lucide React
- Animações e transições suaves
- Layout responsivo (mobile-first)
- Suporte a dark mode (variáveis CSS)

### ✅ Testado

#### Cobertura de Testes (90.58%)
- 123 testes passando
- Framework: Vitest + Testing Library

| Componente | Statements | Branches | Functions |
|------------|------------|----------|-----------|
| App.jsx | 100% | 98.57% | 100% |
| Login.jsx | 97.43% | 97.22% | 100% |
| DashboardView.jsx | 100% | 100% | 100% |
| AgendaView.jsx | 78.87% | 95.00% | 94.11% |
| KanbanView.jsx | 100% | 100% | 100% |

#### Cenários Testados
- Renderização de componentes
- Fluxo de autenticação (login/logout/refresh)
- Navegação entre abas
- Criação e edição de compromissos
- Exclusão com confirmação
- Drag and drop no Kanban
- Tratamento de erros de API
- Estados vazios e edge cases
- Interceptors de API e segurança

### 🔧 Configuração

#### Scripts Disponíveis
```bash
npm run dev        # Servidor de desenvolvimento
npm run build      # Build de produção
npm run preview    # Preview do build
npm run lint       # Verificação de código
npm test           # Executar testes
npm run test:coverage  # Testes com cobertura
```

#### Variáveis de Ambiente
```env
VITE_API_URL=http://localhost:8080
```

### 📦 Dependências Principais
- React 19.x
- React DOM 19.x
- React Router DOM 7.x
- Axios 1.x
- @hello-pangea/dnd 18.x
- Lucide React 0.56.x
- Vite 7.x
- Vitest 2.x
- Testing Library (React, Jest-DOM, User-Event)

### 🐛 Corrigido

- Warnings de `act()` nos testes de componentes assíncronos
- Seletores de elementos em testes de formulário
- Exclusão de arquivos de configuração do relatório de cobertura
- Correção na lógica de retry do interceptor de refresh token

---

## [Unreleased]

### Planejado
- [ ] Notificações push para compromissos
- [ ] Modo offline com sincronização
- [ ] Temas personalizáveis
- [ ] Exportação para PDF/iCal
- [ ] PWA (Progressive Web App)
- [ ] Internacionalização (i18n)

---

[1.0.0-rc.1]: https://github.com/seu-usuario/lifeos/releases/tag/v1.0.0-rc.1-frontend
[1.0.0-beta.1]: https://github.com/seu-usuario/lifeos/releases/tag/v1.0.0-beta.1-frontend
[Unreleased]: https://github.com/seu-usuario/lifeos/compare/v1.0.0-rc.1-frontend...HEAD
