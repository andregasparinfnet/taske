import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import AgendaView from './AgendaView';

describe('AgendaView Component', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const mockCompromissos = [
        {
            id: 1,
            titulo: 'Reunião de Trabalho',
            tipo: 'TRABALHO',
            dataHora: '2026-02-01T10:00',
            status: 'PENDENTE',
            valor: 0,
            descricao: 'Descrição da reunião'
        },
        {
            id: 2,
            titulo: 'Perícia Judicial',
            tipo: 'PERICIA',
            dataHora: '2026-02-02T14:00',
            status: 'EM_ANDAMENTO',
            valor: 500,
            descricao: ''
        },
        {
            id: 3,
            titulo: 'Evento Familiar',
            tipo: 'FAMILIA',
            dataHora: '2026-02-03T09:00',
            status: 'CONCLUIDO',
            valor: 0,
            descricao: 'Aniversário'
        }
    ];

    // ==================== TESTES DE RENDERIZAÇÃO ====================

    it('deve renderizar compromissos com títulos corretos', () => {
        render(<AgendaView compromissos={mockCompromissos} />);

        expect(screen.getByText('Reunião de Trabalho')).toBeInTheDocument();
        expect(screen.getByText('Perícia Judicial')).toBeInTheDocument();
        expect(screen.getByText('Evento Familiar')).toBeInTheDocument();
    });

    it('deve exibir tipos como tags', () => {
        render(<AgendaView compromissos={mockCompromissos} />);

        expect(screen.getByText('TRABALHO')).toBeInTheDocument();
        expect(screen.getByText('PERICIA')).toBeInTheDocument();
        expect(screen.getByText('FAMILIA')).toBeInTheDocument();
    });

    it('deve exibir descrição quando presente', () => {
        render(<AgendaView compromissos={mockCompromissos} />);

        expect(screen.getByText('Descrição da reunião')).toBeInTheDocument();
        expect(screen.getByText('Aniversário')).toBeInTheDocument();
    });

    it('deve exibir "Sem descrição" quando descrição está vazia', () => {
        render(<AgendaView compromissos={mockCompromissos} />);

        expect(screen.getByText('Sem descrição')).toBeInTheDocument();
    });

    it('deve exibir valor quando maior que zero', () => {
        render(<AgendaView compromissos={mockCompromissos} />);

        expect(screen.getByText('R$ 500')).toBeInTheDocument();
    });

    it('deve renderizar view vazia quando não há compromissos', () => {
        render(<AgendaView compromissos={[]} />);

        // Não deve haver nenhum card
        expect(screen.queryByText('Reunião de Trabalho')).not.toBeInTheDocument();
    });

    it('deve exibir horário formatado corretamente', () => {
        render(<AgendaView compromissos={mockCompromissos} />);

        // O horário de 10:00 deve aparecer
        expect(screen.getByText('10:00')).toBeInTheDocument();
    });
});
