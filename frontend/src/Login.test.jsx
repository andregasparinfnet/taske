import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import axios from 'axios';
import Login from './Login';

describe('Login Component', () => {
    const mockOnLogin = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
    });

    // ==================== TESTES DE RENDERIZAÇÃO ====================

    it('deve renderizar formulário de login por padrão', () => {
        render(<Login onLogin={mockOnLogin} />);

        expect(screen.getByText('Bem-vindo ao LifeOS')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Digite seu usuário')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Sua senha')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /entrar/i })).toBeInTheDocument();
    });

    it('deve alternar para modo registro ao clicar no botão', async () => {
        render(<Login onLogin={mockOnLogin} />);

        const toggleButton = screen.getByText(/não tem conta/i);
        await userEvent.click(toggleButton);

        expect(screen.getByText('Criar Conta')).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/ex: ana.silva/i)).toBeInTheDocument();
    });

    // ==================== TESTES DE LOGIN ====================

    it('deve chamar onLogin após sucesso no login', async () => {
        axios.post.mockResolvedValueOnce({
            data: { accessToken: 'jwt-token-teste' }
        });

        render(<Login onLogin={mockOnLogin} />);

        await userEvent.type(screen.getByPlaceholderText('Digite seu usuário'), 'usuario');
        await userEvent.type(screen.getByPlaceholderText('Sua senha'), 'senha123');
        await userEvent.click(screen.getByRole('button', { name: /entrar/i }));

        await waitFor(() => {
            expect(mockOnLogin).toHaveBeenCalledWith({
                username: 'usuario',
                token: 'jwt-token-teste'
            });
        });
    });

    it('deve exibir erro com credenciais inválidas', async () => {
        axios.post.mockRejectedValueOnce(new Error('Unauthorized'));

        render(<Login onLogin={mockOnLogin} />);

        await userEvent.type(screen.getByPlaceholderText('Digite seu usuário'), 'usuario');
        await userEvent.type(screen.getByPlaceholderText('Sua senha'), 'senhaerrada');
        await userEvent.click(screen.getByRole('button', { name: /entrar/i }));

        await waitFor(() => {
            expect(screen.getByText(/credenciais inválidas/i)).toBeInTheDocument();
        });
    });

    // ==================== TESTES DE REGISTRO ====================

    it('deve exibir sucesso após registrar', async () => {
        axios.post.mockResolvedValueOnce({ data: 'Sucesso' });

        render(<Login onLogin={mockOnLogin} />);

        // Alternar para registro
        await userEvent.click(screen.getByText(/não tem conta/i));

        await userEvent.type(screen.getByPlaceholderText(/ex: ana.silva/i), 'novousuario');
        await userEvent.type(screen.getByPlaceholderText(/mínimo de 6 caracteres/i), 'senha123');
        await userEvent.click(screen.getByRole('button', { name: /cadastrar/i }));

        await waitFor(() => {
            expect(screen.getByText(/conta criada com sucesso/i)).toBeInTheDocument();
        });
    });

    it('deve exibir erro se usuário já existe', async () => {
        axios.post.mockRejectedValueOnce({
            response: { data: 'Erro: Nome de usuário já existe!' }
        });

        render(<Login onLogin={mockOnLogin} />);

        await userEvent.click(screen.getByText(/não tem conta/i));

        await userEvent.type(screen.getByPlaceholderText(/ex: ana.silva/i), 'usuarioexistente');
        await userEvent.type(screen.getByPlaceholderText(/mínimo de 6 caracteres/i), 'senha123');
        await userEvent.click(screen.getByRole('button', { name: /cadastrar/i }));

        await waitFor(() => {
            expect(screen.getByText(/já existe/i)).toBeInTheDocument();
        });
    });
});
