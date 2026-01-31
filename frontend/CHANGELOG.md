# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [1.0.0-beta.1] - 2026-01-31

### 🚧 Versão Beta

Primeira versão estável do LifeOS Frontend - Interface React para sistema de gestão pessoal.

### ✨ Adicionado

#### Interface de Autenticação
- Tela de login com validação de campos
- Tela de registro de novos usuários
- Alternância fluida entre login e registro
- Armazenamento seguro de token JWT no localStorage
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

#### Cobertura de Testes (98.01%)
- 44 testes unitários passando
- Framework: Vitest + Testing Library

| Componente | Statements | Branches | Functions |
|------------|------------|----------|-----------|
| App.jsx | 94.52% | 81.25% | 80.00% |
| Login.jsx | 98.42% | 94.11% | 100% |
| DashboardView.jsx | 100% | 100% | 100% |
| AgendaView.jsx | 100% | 91.66% | 100% |
| KanbanView.jsx | 93.16% | 87.50% | 83.33% |

#### Cenários Testados
- Renderização de componentes
- Fluxo de autenticação (login/logout)
- Navegação entre abas
- Criação e edição de compromissos
- Exclusão com confirmação
- Drag and drop no Kanban
- Tratamento de erros de API
- Estados vazios e edge cases

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
- React 19.2.0
- React DOM 19.2.0
- React Router DOM 7.13.0
- Axios 1.13.2
- @hello-pangea/dnd 18.0.1
- Lucide React 0.562.0
- Vite 7.2.4
- Vitest 2.0.0
- Testing Library (React, Jest-DOM, User-Event)

### 🐛 Corrigido

- Warnings de `act()` nos testes de componentes assíncronos
- Seletores de elementos em testes de formulário
- Exclusão de arquivos de configuração do relatório de cobertura

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

[1.0.0-beta.1]: https://github.com/seu-usuario/lifeos/releases/tag/v1.0.0-beta.1-frontend
[Unreleased]: https://github.com/seu-usuario/lifeos/compare/v1.0.0-beta.1-frontend...HEAD
