import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import axios from 'axios';
import KanbanView from './KanbanView';

// Mock do DragDropContext para simular eventos de drag
vi.mock('@hello-pangea/dnd', async () => {
    const actual = await vi.importActual('@hello-pangea/dnd');
    return {
        ...actual,
        DragDropContext: ({ children, onDragEnd }) => {
            // Expor onDragEnd para testes
            window.__testOnDragEnd = onDragEnd;
            return <div data-testid="drag-context">{children}</div>;
        },
        Droppable: ({ children, droppableId }) => (
            <div data-testid={`droppable-${droppableId}`}>
                {children({
                    innerRef: vi.fn(),
                    droppableProps: {},
                    placeholder: null
                }, { isDraggingOver: false })}
            </div>
        ),
        Draggable: ({ children, draggableId }) => (
            <div data-testid={`draggable-${draggableId}`}>
                {children({
                    innerRef: vi.fn(),
                    draggableProps: { style: {} },
                    dragHandleProps: {}
                }, { isDragging: false })}
            </div>
        )
    };
});

describe('KanbanView Component', () => {
    const mockOnUpdate = vi.fn();

    const mockCompromissos = [
        { id: 1, titulo: 'Tarefa Pendente', tipo: 'TRABALHO', dataHora: '2026-02-01T10:00', status: 'PENDENTE', valor: 0 },
        { id: 2, titulo: 'Tarefa em Andamento', tipo: 'PERICIA', dataHora: '2026-02-02T14:00', status: 'EM_ANDAMENTO', valor: 500 },
        { id: 3, titulo: 'Tarefa Concluída', tipo: 'FAMILIA', dataHora: '2026-02-03T09:00', status: 'CONCLUIDO', valor: 0 }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
    });

    // ==================== TESTES DE RENDERIZAÇÃO ====================

    it('deve renderizar 3 colunas do Kanban', () => {
        render(<KanbanView compromissos={[]} onUpdate={mockOnUpdate} />);

        expect(screen.getByText('A Fazer')).toBeInTheDocument();
        expect(screen.getByText('Em Progresso')).toBeInTheDocument();
        expect(screen.getByText('Concluído')).toBeInTheDocument();
    });

    it('deve exibir cards organizados por status', () => {
        render(<KanbanView compromissos={mockCompromissos} onUpdate={mockOnUpdate} />);

        expect(screen.getByText('Tarefa Pendente')).toBeInTheDocument();
        expect(screen.getByText('Tarefa em Andamento')).toBeInTheDocument();
        expect(screen.getByText('Tarefa Concluída')).toBeInTheDocument();
    });

    it('deve exibir contagem de cards por coluna', () => {
        render(<KanbanView compromissos={mockCompromissos} onUpdate={mockOnUpdate} />);

        // Verificar contadores (aparecem como badges)
        const counters = screen.getAllByText('1');
        expect(counters.length).toBeGreaterThanOrEqual(3); // 1 card em cada coluna
    });

    it('deve exibir valor quando maior que zero', () => {
        render(<KanbanView compromissos={mockCompromissos} onUpdate={mockOnUpdate} />);

        expect(screen.getByText('R$ 500.00')).toBeInTheDocument();
    });

    it('deve exibir tipo do compromisso como tag', () => {
        render(<KanbanView compromissos={mockCompromissos} onUpdate={mockOnUpdate} />);

        expect(screen.getByText('TRABALHO')).toBeInTheDocument();
        expect(screen.getByText('PERICIA')).toBeInTheDocument();
        expect(screen.getByText('FAMILIA')).toBeInTheDocument();
    });

    // ==================== TESTES DE DIFERENTES TIPOS ====================

    it('deve exibir card com tipo FINANCEIRO', () => {
        const compromissosComFinanceiro = [
            { id: 4, titulo: 'Pagamento', tipo: 'FINANCEIRO', dataHora: '2026-02-04T10:00', status: 'PENDENTE', valor: 1000 }
        ];
        render(<KanbanView compromissos={compromissosComFinanceiro} onUpdate={mockOnUpdate} />);

        expect(screen.getByText('FINANCEIRO')).toBeInTheDocument();
        expect(screen.getByText('Pagamento')).toBeInTheDocument();
    });

    it('deve exibir card com tipo desconhecido (default)', () => {
        const compromissosComTipoDesconhecido = [
            { id: 5, titulo: 'Outro Compromisso', tipo: 'OUTRO', dataHora: '2026-02-05T10:00', status: 'PENDENTE', valor: 0 }
        ];
        render(<KanbanView compromissos={compromissosComTipoDesconhecido} onUpdate={mockOnUpdate} />);

        expect(screen.getByText('OUTRO')).toBeInTheDocument();
    });

    // ==================== TESTES DE DRAG AND DROP ====================

    it('deve chamar onUpdate ao mover card para outra coluna', async () => {
        axios.put.mockResolvedValue({});

        render(<KanbanView compromissos={mockCompromissos} onUpdate={mockOnUpdate} />);

        // Simular evento de drag end
        const dragResult = {
            destination: { droppableId: 'EM_ANDAMENTO', index: 0 },
            source: { droppableId: 'PENDENTE', index: 0 },
            draggableId: '1'
        };

        // Chamar onDragEnd exposto pelo mock
        if (window.__testOnDragEnd) {
            await window.__testOnDragEnd(dragResult);
        }

        await waitFor(() => {
            expect(mockOnUpdate).toHaveBeenCalledWith(
                expect.objectContaining({
                    id: 1,
                    status: 'EM_ANDAMENTO'
                })
            );
        });

        expect(axios.put).toHaveBeenCalledWith(
            'http://localhost:8080/api/compromissos/1',
            expect.objectContaining({ status: 'EM_ANDAMENTO' })
        );
    });

    it('não deve atualizar se soltar no mesmo local', async () => {
        render(<KanbanView compromissos={mockCompromissos} onUpdate={mockOnUpdate} />);

        const dragResult = {
            destination: { droppableId: 'PENDENTE', index: 0 },
            source: { droppableId: 'PENDENTE', index: 0 },
            draggableId: '1'
        };

        if (window.__testOnDragEnd) {
            await window.__testOnDragEnd(dragResult);
        }

        expect(mockOnUpdate).not.toHaveBeenCalled();
    });

    it('não deve atualizar se destination for null', async () => {
        render(<KanbanView compromissos={mockCompromissos} onUpdate={mockOnUpdate} />);

        const dragResult = {
            destination: null,
            source: { droppableId: 'PENDENTE', index: 0 },
            draggableId: '1'
        };

        if (window.__testOnDragEnd) {
            await window.__testOnDragEnd(dragResult);
        }

        expect(mockOnUpdate).not.toHaveBeenCalled();
    });

    it('deve tratar erro na API ao mover card', async () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
        axios.put.mockRejectedValue(new Error('Erro de rede'));

        render(<KanbanView compromissos={mockCompromissos} onUpdate={mockOnUpdate} />);

        const dragResult = {
            destination: { droppableId: 'CONCLUIDO', index: 0 },
            source: { droppableId: 'PENDENTE', index: 0 },
            draggableId: '1'
        };

        if (window.__testOnDragEnd) {
            await window.__testOnDragEnd(dragResult);
        }

        await waitFor(() => {
            expect(consoleSpy).toHaveBeenCalledWith('Erro ao mover card', expect.any(Error));
        });

        consoleSpy.mockRestore();
    });

    // ==================== TESTES DE CARDS SEM STATUS ====================

    it('deve tratar compromisso sem status como PENDENTE', () => {
        const compromissoSemStatus = [
            { id: 6, titulo: 'Sem Status', tipo: 'TRABALHO', dataHora: '2026-02-06T10:00', valor: 0 }
        ];
        render(<KanbanView compromissos={compromissoSemStatus} onUpdate={mockOnUpdate} />);

        // Deve aparecer na coluna "A Fazer" (PENDENTE)
        expect(screen.getByText('Sem Status')).toBeInTheDocument();
    });
});

