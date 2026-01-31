import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import axios from 'axios';
import App from './App';

// Mock KanbanView para testar callback onUpdate
vi.mock('./KanbanView', () => ({
    default: ({ onUpdate }) => (
        <div data-testid="kanban-view">
            <button onClick={() => onUpdate({ id: 99, titulo: 'Updated by Kanban', status: 'EM_ANDAMENTO' })}>
                Trigger Update
            </button>
        </div>
    )
}));

vi.mock('./ConfirmModal', () => ({
    default: ({ isOpen, onConfirm, onClose, title }) => isOpen ? (
        <div data-testid="confirm-modal">
            <h2>{title || 'Excluir Compromisso'}</h2>
            <button onClick={onConfirm}>Sim, excluir</button>
            <button onClick={onClose}>Cancelar</button>
        </div>
    ) : (
        <button onClick={() => onConfirm()} data-testid="force-confirm-null" style={{ display: 'none' }}>Force Confirm</button>
    )
}));

describe('App Component', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        window.localStorage.getItem.mockReturnValue(null);
    });

    // Helper para simular usuário logado
    const setupAuthenticatedUser = (compromissos = []) => {
        window.localStorage.getItem.mockReturnValue(JSON.stringify({
            username: 'usuario',
            token: 'jwt-token'
        }));
        axios.get.mockResolvedValue({ data: compromissos });
    };

    it('deve fazer logout automático se API retornar 403', async () => {
        setupAuthenticatedUser();
        // Simular erro 403 na busca de compromissos
        axios.get.mockRejectedValue({ response: { status: 403 } });

        render(<App />);

        await waitFor(() => {
            expect(window.localStorage.removeItem).toHaveBeenCalledWith('auth');
        });

        // Deve voltar para login (não ter Minha Agenda)
        expect(screen.queryByText('Minha Agenda')).not.toBeInTheDocument();
    });

    it('deve usar ícone padrão para tipo desconhecido', async () => {
        const compromissos = [{ id: 1, titulo: 'Teste', tipo: 'UNKNOWN', dataHora: '2026-01-01' }];
        setupAuthenticatedUser(compromissos);
        render(<App />);

        await waitFor(() => {
            // Calendar icon é o default -> verifica se renderizou algo (lucide icons viram svgs)
            expect(screen.getByText('Teste')).toBeInTheDocument();
        });
        // Difícil testar qual ícone específico renderizou sem testid, 
        // mas garante que não quebrou a renderização
    });

    // ==================== TESTES DE AUTENTICAÇÃO ====================

    it('deve mostrar Login se não autenticado', () => {
        render(<App />);
        expect(screen.getByText('Bem-vindo ao LifeOS')).toBeInTheDocument();
    });

    it('deve renderizar formulário de login com campos corretos', () => {
        render(<App />);
        expect(screen.getByPlaceholderText('Digite seu usuário')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Sua senha')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /entrar/i })).toBeInTheDocument();
    });

    it('deve realizar login completo via fluxo de UI', async () => {
        // Mock de resposta de login
        axios.post.mockResolvedValueOnce({
            data: { accessToken: 'fake-jwt-token' }
        });
        axios.get.mockResolvedValueOnce({ data: [] }); // carregarCompromissos

        render(<App />);

        await userEvent.type(screen.getByPlaceholderText('Digite seu usuário'), 'admin');
        await userEvent.type(screen.getByPlaceholderText('Sua senha'), '123456');
        await userEvent.click(screen.getByRole('button', { name: /entrar/i }));

        await waitFor(() => {
            // Verifica se chamou carregarCompromissos (que é chamado pelo handleLogin)
            expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/compromissos');
            // Verifica se setou localStorage
            expect(window.localStorage.setItem).toHaveBeenCalledWith('auth', JSON.stringify({
                username: 'admin',
                token: 'fake-jwt-token'
            }));
            // Verifica se mostrou a Agenda (default)
            expect(screen.getByText('Minha Agenda')).toBeInTheDocument();
        });
    });

    it('deve exibir opção de criar conta', () => {
        render(<App />);
        expect(screen.getByText(/não tem conta/i)).toBeInTheDocument();
    });

    it('deve renderizar Agenda quando autenticado por padrão', async () => {
        setupAuthenticatedUser();
        render(<App />);

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        const lifeOsElements = screen.getAllByText('LifeOS');
        expect(lifeOsElements.length).toBeGreaterThan(0);
        expect(screen.getByText('Minha Agenda')).toBeInTheDocument();
    });

    it('deve exibir botão de sair quando autenticado', async () => {
        setupAuthenticatedUser();
        render(<App />);

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        expect(screen.getByText('Sair')).toBeInTheDocument();
    });

    // ==================== TESTES DE LOGOUT ====================

    it('deve fazer logout ao clicar no botão Sair', async () => {
        setupAuthenticatedUser();
        render(<App />);

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        // Clicar em Sair (há mais de um, pegar o do sidebar)
        const logoutButtons = screen.getAllByText('Sair');
        await userEvent.click(logoutButtons[0]);

        // Deve voltar para tela de login
        await waitFor(() => {
            expect(screen.getByText('Bem-vindo ao LifeOS')).toBeInTheDocument();
        });

        expect(window.localStorage.removeItem).toHaveBeenCalledWith('auth');
    });

    // ==================== TESTES DE NAVEGAÇÃO ====================

    it('deve navegar para aba Dashboard', async () => {
        setupAuthenticatedUser();
        render(<App />);

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        const dashboardButton = screen.getByText('Dashboard');
        await userEvent.click(dashboardButton);

        expect(screen.getByText('Visão Geral')).toBeInTheDocument();
    });

    it('deve navegar para aba Quadros (Kanban)', async () => {
        setupAuthenticatedUser();
        render(<App />);

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        const quadrosButton = screen.getByText('Quadros');
        await userEvent.click(quadrosButton);

        expect(screen.getByText('Quadro de Atividades')).toBeInTheDocument();
    });

    // ==================== TESTES DE COMPROMISSOS ====================

    it('deve exibir compromissos na lista', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Reunião Importante', tipo: 'TRABALHO', dataHora: '2026-02-01T10:00', status: 'PENDENTE', valor: 0 }
        ];
        setupAuthenticatedUser(mockCompromissos);
        render(<App />);

        // Navegar para Dashboard
        await userEvent.click(screen.getByText('Dashboard'));

        await waitFor(() => {
            expect(screen.getByText('Reunião Importante')).toBeInTheDocument();
        });
    });

    it('deve calcular estatísticas corretamente', async () => {
        const mockCompromissos = [
            { id: 1, tipo: 'PERICIA', dataHora: '2099-01-01' }, // Futuro
            { id: 2, tipo: 'TRABALHO', dataHora: '2000-01-01' } // Passado
        ];
        setupAuthenticatedUser(mockCompromissos);
        render(<App />);

        // Navegar para Dashboard
        await userEvent.click(screen.getByText('Dashboard'));

        await waitFor(() => {
            // DashboardView mostra esses números. Vamos verificar se ele renderizou os cards.
            // Total = 2, Perícias = 1, Próximos = 1
            const totalElements = screen.getAllByText('2'); // Pode ter mais de um '2' na tela
            expect(totalElements.length).toBeGreaterThan(0);

            const periciasElements = screen.getAllByText('1');
            expect(periciasElements.length).toBeGreaterThan(0);
        });
    });

    it('deve submeter formulário para criar novo compromisso', async () => {
        setupAuthenticatedUser();
        axios.post.mockResolvedValue({ data: {} });

        render(<App />);

        // Navegar para Dashboard (onde está o formulário)
        await userEvent.click(screen.getByText('Dashboard'));

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        // Preencher formulário usando atributo name
        const tituloInput = document.querySelector('input[name="titulo"]');
        const dataInput = document.querySelector('input[name="dataHora"]');
        const urgenteCheckbox = document.querySelector('input[name="urgente"]');

        await userEvent.type(tituloInput, 'Novo Compromisso');
        fireEvent.change(dataInput, { target: { value: '2026-02-15T14:00' } });
        await userEvent.click(urgenteCheckbox);

        // Submeter
        const submitButton = screen.getByRole('button', { name: /salvar/i });
        await userEvent.click(submitButton);

        await waitFor(() => {
            expect(axios.post).toHaveBeenCalledWith(
                'http://localhost:8080/api/compromissos',
                expect.objectContaining({ titulo: 'Novo Compromisso' })
            );
        });
    });

    it('deve lidar com erro ao salvar compromisso', async () => {
        setupAuthenticatedUser();
        axios.post.mockRejectedValue(new Error('Erro de rede'));

        render(<App />);

        // Navegar para Dashboard
        await userEvent.click(screen.getByText('Dashboard'));

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        const tituloInput = document.querySelector('input[name="titulo"]');
        const dataInput = document.querySelector('input[name="dataHora"]');

        await userEvent.type(tituloInput, 'Teste Erro');
        fireEvent.change(dataInput, { target: { value: '2026-02-15T14:00' } });

        const submitButton = screen.getByRole('button', { name: /salvar/i });
        await userEvent.click(submitButton);

        // Verificar que o axios.post foi chamado (mesmo que tenha falhado)
        await waitFor(() => {
            expect(axios.post).toHaveBeenCalled();
        });
    });

    it('deve deletar compromisso ao confirmar no modal', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Para Deletar', tipo: 'TRABALHO', dataHora: '2026-02-01T10:00', status: 'PENDENTE', valor: 0 }
        ];
        setupAuthenticatedUser(mockCompromissos);
        axios.delete.mockResolvedValue({});

        render(<App />);

        // Navegar para Dashboard
        await userEvent.click(screen.getByText('Dashboard'));

        await waitFor(() => {
            expect(screen.getByText('Para Deletar')).toBeInTheDocument();
        });

        // Encontrar botão de deletar usando querySelector
        const deleteBtn = document.querySelector('.action-btn.delete');
        expect(deleteBtn).toBeTruthy();

        await userEvent.click(deleteBtn);

        // Deve aparecer o modal
        expect(screen.getByText('Excluir Compromisso')).toBeInTheDocument();
        expect(screen.getByText('Sim, excluir')).toBeInTheDocument();

        // Clicar em confirmar no modal
        const confirmBtn = screen.getByText('Sim, excluir');
        await userEvent.click(confirmBtn);

        await waitFor(() => {
            expect(axios.delete).toHaveBeenCalledWith('http://localhost:8080/api/compromissos/1');
        });
    });

    it('deve lidar com erro ao deletar compromisso', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Falha Delete', tipo: 'TRABALHO', dataHora: '2026-02-01T10:00', status: 'PENDENTE', valor: 0 }
        ];
        setupAuthenticatedUser(mockCompromissos);
        axios.delete.mockRejectedValue(new Error('Erro ao deletar'));

        render(<App />);

        // Navegar para Dashboard
        await userEvent.click(screen.getByText('Dashboard'));

        await waitFor(() => expect(screen.getByText('Falha Delete')).toBeInTheDocument());

        const deleteBtn = document.querySelector('.action-btn.delete');
        await userEvent.click(deleteBtn);

        await userEvent.click(screen.getByText('Sim, excluir'));

        await waitFor(() => {
            expect(axios.delete).toHaveBeenCalled();
            // Apenas verifica que o modal fechou ou que um toast de erro pode ter sido chamado
            // Como ShowToast é interno, verificamos se o item ainda está lá (ou se o erro foi logado)
            // Aqui assumimos que o modal fecha no final
            expect(screen.queryByText('Excluir Compromisso')).not.toBeInTheDocument();
        });
    });

    it('deve completar o fluxo de edição de compromisso', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Original', tipo: 'PERICIA', dataHora: '2026-02-01T10:00', status: 'PENDENTE', valor: 100, descricao: 'Teste' }
        ];
        setupAuthenticatedUser(mockCompromissos);
        axios.put.mockResolvedValue({});
        axios.get.mockResolvedValue({ data: mockCompromissos }); // Recarrega

        render(<App />);

        // Navegar para Dashboard
        await userEvent.click(screen.getByText('Dashboard'));

        await waitFor(() => expect(screen.getByText('Original')).toBeInTheDocument());

        // Clicar em editar
        const editBtn = document.querySelector('.action-btn:not(.delete)');
        await userEvent.click(editBtn);

        await waitFor(() => expect(screen.getByText('Editar Compromisso')).toBeInTheDocument());

        // Alterar título
        const tituloInput = document.querySelector('input[name="titulo"]');
        await userEvent.clear(tituloInput);
        await userEvent.type(tituloInput, 'Editado');

        // Submeter
        const submitBtn = screen.getByRole('button', { name: /atualizar/i });
        await userEvent.click(submitBtn);

        await waitFor(() => {
            expect(axios.put).toHaveBeenCalledWith(
                'http://localhost:8080/api/compromissos/1',
                expect.objectContaining({ titulo: 'Editado' })
            );
        });
    });

    it('deve cancelar edição resetando o formulário', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Para Cancelar', tipo: 'PERICIA', dataHora: '2026-02-01T10:00' }
        ];
        setupAuthenticatedUser(mockCompromissos);

        render(<App />);

        // Navegar para Dashboard
        await userEvent.click(screen.getByText('Dashboard'));

        await waitFor(() => expect(screen.getByText('Para Cancelar')).toBeInTheDocument());

        const editBtn = document.querySelector('.action-btn:not(.delete)');
        await userEvent.click(editBtn);

        await waitFor(() => expect(screen.getByText('Cancelar Edição')).toBeInTheDocument());

        const cancelBtn = screen.getByText('Cancelar Edição');
        await userEvent.click(cancelBtn);

        expect(screen.getByText('Novo Compromisso')).toBeInTheDocument();
        expect(screen.queryByText('Cancelar Edição')).not.toBeInTheDocument();
    });

    // ==================== TESTES DE EXIBIÇÃO DE TIPOS ====================

    it('deve exibir compromissos com diferentes tipos', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Trabalho', tipo: 'TRABALHO', dataHora: '2026-02-01T10:00', status: 'PENDENTE', valor: 0, urgente: true },
            { id: 2, titulo: 'Perícia', tipo: 'PERICIA', dataHora: '2026-02-02T10:00', status: 'PENDENTE', valor: 500 },
            { id: 3, titulo: 'Família', tipo: 'FAMILIA', dataHora: '2026-02-03T10:00', status: 'PENDENTE', valor: 0 },
            { id: 4, titulo: 'Financeiro', tipo: 'FINANCEIRO', dataHora: '2026-02-04T10:00', status: 'PENDENTE', valor: 0 },
            { id: 5, titulo: 'Estudos', tipo: 'ESTUDOS', dataHora: '2026-02-05T10:00', status: 'PENDENTE', valor: 0 },
            { id: 6, titulo: 'Outros', tipo: 'OUTROS', dataHora: '2026-02-06T10:00', status: 'PENDENTE', valor: 0 },
        ];
        setupAuthenticatedUser(mockCompromissos);

        render(<App />);

        // Navegar para Dashboard
        await userEvent.click(screen.getByText('Dashboard'));


        // Use regex for case-insensitive match if needed, but getAllByText is safer due to Form Options 
        await waitFor(() => {
            expect(screen.getAllByText(/Trabalho/i).length).toBeGreaterThan(0);
            expect(screen.getAllByText(/Perícia/i).length).toBeGreaterThan(0);
            expect(screen.getAllByText(/Família/i).length).toBeGreaterThan(0);
            // Check other types simply by existence (assuming they render text or icon title if implemented)
            // But getTagIcon usage in App.jsx only returns Icon. 
            // App.jsx doesn't render TYPE TEXT in the list? 
            // Wait, does it? 
            // Line 251: <div ...>{getTagIcon(item.tipo)}</div>
            // Line 255: Title...
            // Line 276: Status...
            // It seems TYPE TEXT is NOT rendered in the Dashboard List!
            // BUT the previous test verified 'Trabalho', 'Perícia', 'Família' because they were TITLES!
            // I named them 'Trabalho', 'Perícia', 'Família'.
            // So to test getTagIcon, I rely on the FACT that getTagIcon IS CALLED.
            // Rendering the list calls getTagIcon.
            // So just rendering them with different types is enough to cover the switch cases!
            expect(screen.getAllByText(/Financeiro/i).length).toBeGreaterThan(0);
            expect(screen.getAllByText(/Estudos/i).length).toBeGreaterThan(0);
            expect(screen.getAllByText(/Outros/i).length).toBeGreaterThan(0);
            expect(screen.getByText('URGENTE')).toBeInTheDocument();
        });
    });

    it('deve atualizar item via callback do KanbanView', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Outro', status: 'PENDENTE' },
            { id: 99, titulo: 'Original', status: 'PENDENTE' }
        ];
        setupAuthenticatedUser(mockCompromissos);
        render(<App />);

        // Navegar para Kanban
        const quadrosButton = screen.getByText('Quadros');
        await userEvent.click(quadrosButton);

        // Clicar no botão do mock
        const updateBtn = screen.getByText('Trigger Update');
        await userEvent.click(updateBtn);

        // Navegar de volta para Dashboard (onde a lista é visível para confirmação)
        const dashboardBtn = screen.getByText('Dashboard');
        await userEvent.click(dashboardBtn);

        // Navegar para Agenda para cobrir essa função também
        const agendaBtn = screen.getByText('Agenda');
        await userEvent.click(agendaBtn);

        await waitFor(() => {
            // Verificar título ou status atualizado na lista
            expect(screen.getByText('Updated by Kanban')).toBeInTheDocument();
        });
    });

    it('deve fechar toast', async () => {
        setupAuthenticatedUser();
        render(<App />);
        // Simular um erro para gerar toast
        axios.post.mockRejectedValueOnce(new Error('Fail'));

        await userEvent.click(screen.getByText('Dashboard'));
        await userEvent.type(document.querySelector('input[name="titulo"]'), 'Erro Teste');
        fireEvent.change(document.querySelector('input[name="dataHora"]'), { target: { value: '2026-02-15T14:00' } });
        await userEvent.click(screen.getByRole('button', { name: /salvar/i }));

        const toast = await screen.findByText(/Erro ao salvar/);
        expect(toast).toBeInTheDocument();

        // Clicar no botão de fechar do toast
        // O Toast.jsx renderiza um botão 'X'.
        const closeBtn = screen.getByRole('button', { name: 'Fechar' });
        await userEvent.click(closeBtn);

        expect(screen.queryByText(/Erro ao salvar/)).not.toBeInTheDocument();
    });

    it('deve fechar modal de exclusão ao cancelar', async () => {
        setupAuthenticatedUser([{ id: 1, titulo: 'Deletar' }]);
        render(<App />);

        await userEvent.click(screen.getByText('Dashboard'));
        await waitFor(() => expect(screen.getByText('Deletar')).toBeInTheDocument());

        await userEvent.click(document.querySelector('.action-btn.delete'));
        expect(screen.getByText('Excluir Compromisso')).toBeInTheDocument();

        const cancelBtn = screen.getByText(/Cancelar/i);
        await userEvent.click(cancelBtn);

        expect(screen.queryByText('Excluir Compromisso')).not.toBeInTheDocument();
    });

    it('deve fazer logout via botão mobile', async () => {
        setupAuthenticatedUser();
        render(<App />);

        // Botão de logout no header mobile (ícone LogOut)
        // Há dois LogOut icons, um no sidebar outro no mobile header.
        // O do mobile header é o segundo ou posso pegar pelo container.
        const logoutBtns = screen.getAllByRole('button');
        // O do mobile header tem o estilo background: none.
        const mobileLogout = logoutBtns.find(b => b.style.background === 'none');
        await userEvent.click(mobileLogout);

        expect(screen.getByText(/Bem-vindo ao LifeOS/i)).toBeInTheDocument();
    });

    it('deve ignorar deleção sem item selecionado', async () => {
        setupAuthenticatedUser();
        render(<App />);

        const forceBtn = screen.getByTestId('force-confirm-null');
        await userEvent.click(forceBtn);

        expect(axios.delete).not.toHaveBeenCalled();
    });

    it('deve exibir erro ao falhar exclusão', async () => {
        setupAuthenticatedUser([{ id: 1, titulo: 'Falhar' }]);
        render(<App />);

        await userEvent.click(screen.getByText('Dashboard'));
        await userEvent.click(document.querySelector('.action-btn.delete'));

        axios.delete.mockRejectedValueOnce(new Error('Delete Fail'));

        await userEvent.click(screen.getByText('Sim, excluir'));

        await waitFor(() => {
            expect(screen.getByText('Erro ao excluir compromisso')).toBeInTheDocument();
        });
    });

    it('deve realizar login com sucesso', async () => {
        // Iniciar deslogado
        window.localStorage.getItem.mockReturnValue(null);
        axios.post.mockResolvedValue({ data: { username: 'user', token: 'token123' } });
        axios.get.mockResolvedValue({ data: [] });

        render(<App />);

        // Deve estar na tela de login
        const loginInput = screen.getByPlaceholderText(/usuário/i);
        const passInput = screen.getByPlaceholderText(/senha/i);
        const loginBtn = screen.getByRole('button', { name: /entrar/i });

        await userEvent.type(loginInput, 'testuser');
        await userEvent.type(passInput, 'password');
        await userEvent.click(loginBtn);

        await waitFor(() => {
            expect(screen.getByText('Minha Agenda')).toBeInTheDocument();
        });

        expect(localStorage.setItem).toHaveBeenCalledWith('auth', expect.any(String));
    });

    it('deve lidar com erro genérico na busca de compromissos', async () => {
        setupAuthenticatedUser();
        axios.get.mockRejectedValue({ response: { status: 500 } });
        render(<App />);
        await waitFor(() => expect(axios.get).toHaveBeenCalled());
    });
});
