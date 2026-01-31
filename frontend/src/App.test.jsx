import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import axios from 'axios';
import App from './App';

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

    it('deve exibir opção de criar conta', () => {
        render(<App />);
        expect(screen.getByText(/não tem conta/i)).toBeInTheDocument();
    });

    it('deve renderizar Dashboard quando autenticado', async () => {
        setupAuthenticatedUser();
        render(<App />);

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        const lifeOsElements = screen.getAllByText('LifeOS');
        expect(lifeOsElements.length).toBeGreaterThan(0);
        expect(screen.getByText('Dashboard')).toBeInTheDocument();
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

    it('deve navegar para aba Agenda', async () => {
        setupAuthenticatedUser();
        render(<App />);

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        const agendaButton = screen.getByText('Agenda');
        await userEvent.click(agendaButton);

        expect(screen.getByText('Minha Agenda')).toBeInTheDocument();
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

        await waitFor(() => {
            expect(screen.getByText('Reunião Importante')).toBeInTheDocument();
        });
    });

    it('deve submeter formulário para criar novo compromisso', async () => {
        setupAuthenticatedUser();
        axios.post.mockResolvedValue({ data: {} });

        render(<App />);

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalled();
        });

        // Preencher formulário usando atributo name
        const tituloInput = document.querySelector('input[name="titulo"]');
        const dataInput = document.querySelector('input[name="dataHora"]');

        await userEvent.type(tituloInput, 'Novo Compromisso');
        fireEvent.change(dataInput, { target: { value: '2026-02-15T14:00' } });

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

    it('deve deletar compromisso ao confirmar', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Para Deletar', tipo: 'TRABALHO', dataHora: '2026-02-01T10:00', status: 'PENDENTE', valor: 0 }
        ];
        setupAuthenticatedUser(mockCompromissos);
        axios.delete.mockResolvedValue({});
        window.confirm.mockReturnValue(true);

        render(<App />);

        await waitFor(() => {
            expect(screen.getByText('Para Deletar')).toBeInTheDocument();
        });

        // Encontrar botão de deletar usando querySelector
        const deleteBtn = document.querySelector('.action-btn.delete');
        expect(deleteBtn).toBeTruthy();

        await userEvent.click(deleteBtn);

        await waitFor(() => {
            expect(axios.delete).toHaveBeenCalledWith('http://localhost:8080/api/compromissos/1');
        });
    });

    it('deve editar compromisso ao clicar em editar', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Para Editar', tipo: 'PERICIA', dataHora: '2026-02-01T10:00', status: 'PENDENTE', valor: 100, descricao: 'Teste' }
        ];
        setupAuthenticatedUser(mockCompromissos);
        axios.put.mockResolvedValue({});

        render(<App />);

        await waitFor(() => {
            expect(screen.getByText('Para Editar')).toBeInTheDocument();
        });

        // Encontrar e clicar no botão de editar (primeiro action-btn que não é delete)
        const editBtn = document.querySelector('.action-btn:not(.delete)');
        expect(editBtn).toBeTruthy();

        await userEvent.click(editBtn);

        // O formulário deve mostrar "Editar" no header
        await waitFor(() => {
            expect(screen.getByText('Editar')).toBeInTheDocument();
        });
    });

    // ==================== TESTES DE EXIBIÇÃO DE TIPOS ====================

    it('deve exibir compromissos com diferentes tipos', async () => {
        const mockCompromissos = [
            { id: 1, titulo: 'Trabalho', tipo: 'TRABALHO', dataHora: '2026-02-01T10:00', status: 'PENDENTE', valor: 0 },
            { id: 2, titulo: 'Perícia', tipo: 'PERICIA', dataHora: '2026-02-02T10:00', status: 'PENDENTE', valor: 500 },
            { id: 3, titulo: 'Família', tipo: 'FAMILIA', dataHora: '2026-02-03T10:00', status: 'PENDENTE', valor: 0 },
        ];
        setupAuthenticatedUser(mockCompromissos);

        render(<App />);

        await waitFor(() => {
            expect(screen.getByText('Trabalho')).toBeInTheDocument();
            expect(screen.getByText('Perícia')).toBeInTheDocument();
            expect(screen.getByText('Família')).toBeInTheDocument();
        });
    });
});
