import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
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
    it('deve exibir indicador de urgente', () => {
        const compromissosUrgentes = [{
            id: 1,
            titulo: 'Tarefa Importante',
            dataHora: new Date().toISOString(),
            urgente: true
        }];

        render(<AgendaView compromissos={compromissosUrgentes} />);

        expect(screen.getByText(/URGENTE/i)).toBeInTheDocument();
    });

    it('deve agrupar compromissos de hoje separadamente', () => {
        const hoje = new Date();
        const amanha = new Date(hoje);
        amanha.setDate(hoje.getDate() + 1);

        const compromissos = [
            { id: 1, titulo: 'Hoje 1', dataHora: hoje.toISOString(), tipo: 'OUTROS' },
            { id: 2, titulo: 'Amanhã 1', dataHora: amanha.toISOString(), tipo: 'OUTROS' }
        ];

        render(<AgendaView compromissos={compromissos} />);

        expect(screen.getAllByText('Hoje').length).toBeGreaterThan(0);
        expect(screen.getByText('Hoje 1')).toBeInTheDocument();
        expect(screen.getByText('Amanhã 1')).toBeInTheDocument();
    });

    it('deve ordenar compromissos por horário', () => {
        const baseDate = '2026-03-20';
        const compromissos = [
            { id: 1, titulo: 'Tarde', dataHora: `${baseDate}T15:00:00`, tipo: 'OUTROS' },
            { id: 2, titulo: 'Manhã', dataHora: `${baseDate}T09:00:00`, tipo: 'OUTROS' },
            { id: 3, titulo: 'Noite', dataHora: `${baseDate}T20:00:00`, tipo: 'OUTROS' }
        ];

        render(<AgendaView compromissos={compromissos} />);

        const items = screen.getAllByRole('heading', { level: 4 });
        expect(items[0]).toHaveTextContent('Manhã');
        expect(items[1]).toHaveTextContent('Tarde');
        expect(items[2]).toHaveTextContent('Noite');
    });

    // ==================== TESTES DE FILTRAGEM ====================

    it('deve filtrar compromissos por Hoje, 7 dias e 15 dias', async () => {
        const today = new Date();
        const day5 = new Date(today); day5.setDate(today.getDate() + 5);
        const day10 = new Date(today); day10.setDate(today.getDate() + 10);
        const day20 = new Date(today); day20.setDate(today.getDate() + 20);

        const compromissos = [
            { id: 101, titulo: 'Compromisso Hoje', dataHora: today.toISOString(), tipo: 'OUTROS' },
            { id: 102, titulo: 'Compromisso 5 dias', dataHora: day5.toISOString(), tipo: 'OUTROS' },
            { id: 103, titulo: 'Compromisso 10 dias', dataHora: day10.toISOString(), tipo: 'OUTROS' },
            { id: 104, titulo: 'Compromisso 20 dias', dataHora: day20.toISOString(), tipo: 'OUTROS' }
        ];

        // Ensure distinct times to avoid sorting issues if dates are identical (though here dates are distinct days)
        render(<AgendaView compromissos={compromissos} />);

        // Inicialmente (filrtro 'all'), todos devem aparecer
        expect(screen.getByText('Compromisso Hoje')).toBeInTheDocument();
        expect(screen.getAllByText('Compromisso 5 dias').length).toBeGreaterThan(0);
        expect(screen.getAllByText('Compromisso 10 dias').length).toBeGreaterThan(0);
        expect(screen.getAllByText('Compromisso 20 dias').length).toBeGreaterThan(0);

        // Click "Hoje" - Apenas hoje
        // The text for the button/card is "Hoje", but "Hoje" is also a group header.
        // The stat-card h3 is "Hoje". There might be multiple elements with "Hoje".
        // Use more specific query or test-id if needed. But "Hoje" header is only present if there acts are for today.
        // Let's assume userEvent click finds the first one or we use specific selector.
        // Better: userEvent.click(screen.getAllByText('Hoje')[0]) might be risky.
        // The stat card has an h3.

        // Let's find the card by role or text within stat-info
        // Since we don't have test-ids, let's rely on the structure or just click "Hoje" text which is in the button.
        // Note: The group header is also "Hoje". Currently there is a group header "Hoje" because item 101 is today.
        // So there are at least two "Hoje" texts.
        // We can target the one inside "stat-info".
        // Or simply click the element containing count.

        // Let's try to query by text "Hoje" and pick the one that is a heading inside the button?
        const hojeTexts = screen.getAllByText('Hoje');
        // Likely the first one is the card if it renders top to bottom.
        await userEvent.click(hojeTexts[0]);

        expect(screen.getByText('Compromisso Hoje')).toBeInTheDocument();
        expect(screen.queryByText('Compromisso 5 dias')).not.toBeInTheDocument();
        expect(screen.queryByText('Compromisso 10 dias')).not.toBeInTheDocument();

        // Click "Próximos 7 dias"
        await userEvent.click(screen.getByText('Próximos 7 dias'));
        expect(screen.getByText('Compromisso Hoje')).toBeInTheDocument();
        expect(screen.getByText('Compromisso 5 dias')).toBeInTheDocument();
        expect(screen.queryByText('Compromisso 10 dias')).not.toBeInTheDocument(); // 10 days > 7

        // Click "Próximos 15 dias"
        await userEvent.click(screen.getByText('Próximos 15 dias'));
        expect(screen.getByText('Compromisso 10 dias')).toBeInTheDocument();
        expect(screen.queryByText('Compromisso 20 dias')).not.toBeInTheDocument(); // 20 > 15
    });

    it('deve desativar filtro ao clicar novamente', async () => {
        const today = new Date();
        const day5 = new Date(today); day5.setDate(today.getDate() + 5);
        const day10 = new Date(today); day10.setDate(today.getDate() + 10);
        const day20 = new Date(today); day20.setDate(today.getDate() + 20);

        const compromissos = [
            { id: 101, titulo: 'Compromisso Hoje', dataHora: today.toISOString(), tipo: 'OUTROS' },
            { id: 102, titulo: 'Compromisso 5 dias', dataHora: day5.toISOString(), tipo: 'OUTROS' },
            { id: 103, titulo: 'Compromisso 10 dias', dataHora: day10.toISOString(), tipo: 'OUTROS' },
            { id: 104, titulo: 'Compromisso 20 dias', dataHora: day20.toISOString(), tipo: 'OUTROS' }
        ];

        render(<AgendaView compromissos={compromissos} />);

        const hojeTexts = screen.getAllByText('Hoje'); // Get the "Hoje" filter button

        // Click "Hoje" to activate filter
        await userEvent.click(hojeTexts[0]);
        expect(screen.getByText('Compromisso Hoje')).toBeInTheDocument();
        expect(screen.queryByText('Compromisso 5 dias')).not.toBeInTheDocument();

        // Click "Hoje" again to deactivate filter
        await userEvent.click(hojeTexts[0]);
        expect(screen.getByText('Compromisso Hoje')).toBeInTheDocument();
        expect(screen.getByText('Compromisso 5 dias')).toBeInTheDocument(); // Should be visible again
        expect(screen.getByText('Compromisso 10 dias')).toBeInTheDocument();
        expect(screen.getByText('Compromisso 20 dias')).toBeInTheDocument();
    });

    it('deve exibir mensagem de vazio e limpar filtros', async () => {
        const futureDate = new Date();
        futureDate.setFullYear(futureDate.getFullYear() + 1);

        const compromissos = [{ id: 1, titulo: 'Futuro', dataHora: futureDate.toISOString(), tipo: 'OUTROS' }];
        render(<AgendaView compromissos={compromissos} />);

        // Filter Today (button)
        // Since there are no commitments today, the group header "Hoje" will NOT render.
        // So getAllByText('Hoje') will only find the button (and maybe 'Hoje' inside button count? No count is distinct p tag).
        // Actually stat-info -> h3 -> Hoje. 
        // So only 1 element 'Hoje'.
        await userEvent.click(screen.getByText('Hoje'));

        expect(screen.getByText('Nenhum compromisso encontrado para este filtro.')).toBeInTheDocument();

        // Click Clear
        await userEvent.click(screen.getByText('Limpar filtros'));

        expect(screen.getByText('Futuro')).toBeInTheDocument();
    });

    it('deve mostrar todos ao clicar em Todos', async () => {
        const today = new Date();
        const futureDate = new Date();
        futureDate.setFullYear(futureDate.getFullYear() + 1);

        const compromissos = [
            { id: 1, titulo: 'Hoje', dataHora: today.toISOString(), tipo: 'OUTROS' },
            { id: 2, titulo: 'Futuro', dataHora: futureDate.toISOString(), tipo: 'OUTROS' }
        ];
        render(<AgendaView compromissos={compromissos} />);

        // Filter Today
        await userEvent.click(screen.getAllByText('Hoje')[0]);
        expect(screen.getAllByText('Hoje').length).toBeGreaterThan(0);
        expect(screen.queryByText('Futuro')).not.toBeInTheDocument();

        // Click Todos
        await userEvent.click(screen.getByText('Todos'));
        expect(screen.getAllByText('Hoje').length).toBeGreaterThan(0);
        expect(screen.getByText('Futuro')).toBeInTheDocument();
    });
});
