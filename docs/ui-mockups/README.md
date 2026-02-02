# UI Mockups Detalhados (sem Figma)

Objetivo
Fornecer mockups detalhados e autocontidos, sem dependência de Figma, cobrindo telas principais, fluxos, tokens de design e responsividade. Este documento serve como base visual e funcional para as próximas etapas do projeto.

Sumário
- Tokens de Design e Breakpoints
- Padrões Globais (Navegação, Layout, Acessibilidade)
- Telas
  - Login
  - Dashboard
  - Agenda
  - Kanban
  - Modais (Confirm/Toast)
- Fluxos (Auth, CRUD, Estados, Erros)
- Checklist de Entrega

1) Tokens de Design (alinhados ao projeto)
- Cores (Brand e Semânticas)
  - Primária: #4F46E5 (hover: #4338CA; light: #EEF2FF)
  - Texto: principal #111827; secundário #4B5563; mutado #9CA3AF
  - Neutros: bg-body #F9FAFB; bg-surface #FFFFFF; border #E5E7EB (#D1D5DB hover)
  - Semânticas: danger #EF4444 (bg #FEF2F2); success #10B981 (bg #ECFDF5); info #3B82F6 (bg #EFF6FF); warning #F59E0B (bg #FFFBEB)
- Tipografia
  - Fam��lia: Inter, sans-serif
  - Pesos: 700 (títulos), 600 (subtítulos), 500 (labels), 400 (texto)
  - Tamanhos usados: 0.75rem, 0.875rem, 0.95rem, 1rem, 1.125rem, 1.5rem, 1.75rem, 1.875rem
- Espaçamentos e Radius
  - Radius: 8px (sm), 12px (md), 16px (lg), 24px (xl)
  - Sombreamento: sm/md/lg/float conforme index.css
- Transições
  - all 0.2s cubic-bezier(0.4, 0, 0.2, 1)

2) Breakpoints e Responsividade
- Desktop: ≥ 1024px — Sidebar à esquerda; conteúdo à direita
- Tablet: 768–1023px — Sidebar colapsa; grid responsivo
- Mobile: < 768px — Navegação vira barra inferior (70px); conteúdo ocupa altura restante

3) Padrões Globais
- Navegação
  - Desktop: Sidebar com logo, itens (Dashboard, Agenda, Kanban). Item ativo com fundo var(--primary-light) e texto var(--primary).
  - Mobile: Bottom bar com ícones e labels reduzidos.
- Layout Geral
  - Container: grid com Sidebar + Main (desktop), responsivo conforme App.css
  - Main possui header com título/subtítulo e ações (botões primários/secundários)
- Acessibilidade
  - Contrastes AA; foco visível; navegação por teclado nas modais; labels e mensagens de erro claras.

4) Telas (wireframes em texto)

4.1) Login

+--------------------------------------------------------+
|                    Fundo gradiente                     |
|      [Logo]                                            |
|      "Bem-vindo" (h1)                                  |
|      "Faça login para continuar" (p)                   |
|                                                        |
|  [Banner de erro/sucesso opcional]                     |
|                                                        |
|  Label "Usuário"                                       |
|  [ ícone ] [__________________________]                |
|                                                        |
|  Label "Senha"                                         |
|  [ ícone ] [__________________________]                |
|                                                        |
|  ( ) Mostrar senha   [ Esqueci minha senha ]           |
|                                                        |
|  [ Entrar ▶ ]                                          |
|                                                        |
|  [ Criar conta ]                                       |
+--------------------------------------------------------+

Estados:
- Idle: campos vazios; botão desabilitado se inválido.
- Erro: banner .message-banner.error; borda dos inputs em danger.
- Sucesso: banner .message-banner.success (ex. pós-registro).
- Loading: botão com spinner; inputs desativados.

Validações:
- Usuário e senha obrigatórios; mensagem sob o campo.

4.2) Dashboard

+-----------------------------------+--------------------+
|           Header (titulo)         |  [ + Novo ] [ ... ]|
+--------------------------------------------------------+
|    Cards de métricas (grid auto-fit min 240px)         |
|  [Total] [Pendentes] [Concluídos] [Urgentes]          |
+--------------------------------------------------------+
|               Lista recente de compromissos            |
|  [Item]  [Ações: editar | deletar]                     |
|  [Item]  [Ações]                                       |
+--------------------------------------------------------+

Estados:
- Vazio: mensagem "Sem dados ainda" + CTA para criar.
- Erro: toast-error e/ou mensagem inline.

4.3) Agenda (Lista/CRUD)

+--------------------+-----------------------------------+
|  Filtros:          |   Lista de Compromissos           |
|  [Status ▼]        |  ┌──────────────────────────────┐ |
|  [Tipo ▼] [Data]   |  | [Icon] Título (Tag)          | |
|  [Pesquisar ◀]     |  | Descrição (resumo)           | |
|                    |  | [Editar] [Excluir]           | |
|                    |  └──────────────────────────────┘ |
+--------------------+-----------------------------------+

Uso de tags (cores semânticas):
- PERICIA: danger-bg/danger; TRABALHO: info; FAMILIA: #8B5CF6; FINANCEIRO: warning; ESTUDOS: #DB2777; OUTROS: neutro

Estados:
- Vazio: "Nenhum compromisso encontrado" + CTA
- Erro: toast-error; itens com falha de carregamento exibem esqueleto

Ações:
- Criar: abre formulário (inline ou modal) com validações (@Valid)
- Editar: mesma UI de criar; persistência com updateCompromisso
- Excluir: modal de confirmação

4.4) Kanban (Fluxo por status)

+----------------+----------------+----------------+----------------+
|  Pendente      | Em andamento   | Concluído      | Arquivado      |
|  [Card]        | [Card]         | [Card]         | [Card]         |
|  [Card]        | [Card]         |                |                |
+----------------+----------------+----------------+----------------+

- Drag & drop de cards (opcional) para alterar status
- Cards exibem título, tag (tipo), prioridade (se houver), ações rápidas
- Mobile: colunas viram uma lista com filtro de status no topo

4.5) Modais (Confirm/Toast)

Modal de confirmação (excluir):
+---------------------------------------------+
|  (●)  Excluir compromisso?                  |
|       Esta ação não pode ser desfeita.      |
|                                             |
|  [ Cancelar ]      [ Excluir ]              |
+---------------------------------------------+

Toast:
- Sucesso: borda e ícone success (#10B981)
- Erro: borda e ícone danger (#EF4444)
- Posição: canto superior direito

5) Fluxos e Regras
- Auth
  - Login: validações, banner de erro, token em memória, withCredentials para CSRF; refresh automático 401→/auth/refresh.
  - Logout: confirma em modal; limpa estado e chama endpoint; tokens revogados no backend.
- CRUD
  - Create: formulário validado (@Valid), feedback via toast-success.
  - Read: filtragem por usuário, paginação opcional, estados vazios.
  - Update/Delete: ownership check (403 se não pertence), modal em delete.
- Erros e Estados Especiais
  - 401/403: redirect login ou mensagem "Acesso negado"; não vazar detalhes sensíveis.
  - Loading: botões desativados, spinners, skeletons na lista.

6) Checklist de Entrega (sem Figma)
- [ ] Este documento serve como guia de mockup textual e visual.
- [ ] As telas acima estão detalhadas com layout, componentes e estados.
- [ ] Tokens de design e breakpoints definidos e coerentes com o CSS do projeto.
- [ ] Fluxos de Auth e CRUD descritos (incl. mensagens e validações).
- [ ] Acessibilidade considerada (foco, contraste, labels, teclado em modal).

Observações
- Este mockup textual dispensa Figma e pode ser versionado junto ao código.
- Para visualização mais próxima do produto, é possível criar páginas HTML estáticas de protótipo dentro de docs/ui-mockups/prototypes/ reusando os tokens de index.css (opcional).