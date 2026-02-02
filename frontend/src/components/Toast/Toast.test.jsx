import { describe, it, expect, vi } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Toast from './Toast';

describe('Toast Component', () => {
    it('deve renderizar a mensagem corretamente', () => {
        render(<Toast message="Sucesso!" type="success" onClose={() => { }} />);
        expect(screen.getByText('Sucesso!')).toBeInTheDocument();
    });

    it('deve renderizar ícone de erro quando type="error"', () => {
        const { container } = render(<Toast message="Erro!" type="error" onClose={() => { }} />);
        expect(container.firstChild).toHaveClass('toast-error');
    });

    it('deve chamar onClose ao clicar no botão de fechar', async () => {
        const onCloseMock = vi.fn();
        render(<Toast message="Teste" onClose={onCloseMock} />);

        const closeBtn = screen.getByRole('button');
        await userEvent.click(closeBtn);

        expect(onCloseMock).toHaveBeenCalled();
    });

    it('deve chamar onClose automaticamente após 3 segundos', () => {
        vi.useFakeTimers();
        const onCloseMock = vi.fn();

        render(<Toast message="Auto Close" onClose={onCloseMock} />);

        // Ainda não deve ter chamado
        expect(onCloseMock).not.toHaveBeenCalled();

        // Avançar tempo
        act(() => {
            vi.advanceTimersByTime(5000);
        });

        expect(onCloseMock).toHaveBeenCalled();

        vi.useRealTimers();
    });

    it('deve limpar o timer ao desmontar', () => {
        vi.useFakeTimers();
        const onCloseMock = vi.fn();

        const { unmount } = render(<Toast message="Unmount" onClose={onCloseMock} />);

        unmount();

        act(() => {
            vi.advanceTimersByTime(3000);
        });

        // Não deve chamar se foi desmontado antes
        expect(onCloseMock).not.toHaveBeenCalled();

        vi.useRealTimers();
    });
});
