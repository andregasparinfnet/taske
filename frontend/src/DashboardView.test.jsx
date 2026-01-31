import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import DashboardView from './DashboardView';

describe('DashboardView Component', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    // ==================== TESTES DE RENDERIZAÇÃO ====================

    it('deve renderizar cards de estatísticas', () => {
        const mockStats = { total: 10, pericias: 3, proximos: 5 };
        render(<DashboardView stats={mockStats} />);

        expect(screen.getByText('Total')).toBeInTheDocument();
        expect(screen.getByText('Perícias')).toBeInTheDocument();
        expect(screen.getByText('Próximos')).toBeInTheDocument();
    });

    it('deve exibir valores corretos das estatísticas', () => {
        const mockStats = { total: 15, pericias: 7, proximos: 3 };
        render(<DashboardView stats={mockStats} />);

        expect(screen.getByText('15')).toBeInTheDocument();
        expect(screen.getByText('7')).toBeInTheDocument();
        expect(screen.getByText('3')).toBeInTheDocument();
    });

    it('deve exibir widget de boas-vindas', () => {
        const mockStats = { total: 5, pericias: 2, proximos: 4 };
        render(<DashboardView stats={mockStats} />);

        expect(screen.getByText('Painel de Controle')).toBeInTheDocument();
    });

    it('deve exibir contador de próximas atividades no widget', () => {
        const mockStats = { total: 10, pericias: 3, proximos: 8 };
        render(<DashboardView stats={mockStats} />);

        expect(screen.getByText(/8 atividades agendadas/i)).toBeInTheDocument();
    });

    it('deve renderizar com stats zerados', () => {
        const mockStats = { total: 0, pericias: 0, proximos: 0 };
        render(<DashboardView stats={mockStats} />);

        // Verificar que todos os zeros aparecem (3 zeros)
        const zeros = screen.getAllByText('0');
        expect(zeros.length).toBe(3);
        expect(screen.getByText(/0 atividades agendadas/i)).toBeInTheDocument();
    });
});
