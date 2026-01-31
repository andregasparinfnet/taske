import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ConfirmModal from './ConfirmModal';

describe('ConfirmModal Component', () => {
    it('não deve renderizar nada se isOpen for false', () => {
        const { container } = render(<ConfirmModal isOpen={false} />);
        expect(container).toBeEmptyDOMElement();
    });

    it('deve renderizar título e mensagem quando aberto', () => {
        render(
            <ConfirmModal
                isOpen={true}
                title="Título Teste"
                message="Mensagem Teste"
            />
        );
        expect(screen.getByText('Título Teste')).toBeInTheDocument();
        expect(screen.getByText('Mensagem Teste')).toBeInTheDocument();
    });

    it('deve chamar onClose ao clicar em Cancelar', async () => {
        const onCloseMock = vi.fn();
        render(<ConfirmModal isOpen={true} onClose={onCloseMock} title="T" message="M" />);

        await userEvent.click(screen.getByText('Cancelar'));
        expect(onCloseMock).toHaveBeenCalled();
    });

    it('deve chamar onConfirm ao clicar em Confirmar', async () => {
        const onConfirmMock = vi.fn();
        render(<ConfirmModal isOpen={true} onConfirm={onConfirmMock} title="T" message="M" />);

        await userEvent.click(screen.getByText('Sim, excluir'));
        expect(onConfirmMock).toHaveBeenCalled();
    });

    it('deve fechar ao clicar no overlay', async () => {
        const onCloseMock = vi.fn();
        const { container } = render(<ConfirmModal isOpen={true} onClose={onCloseMock} title="T" message="M" />);

        // O primeiro div é o overlay
        const overlay = container.firstChild;
        await userEvent.click(overlay);

        expect(onCloseMock).toHaveBeenCalled();
    });

    it('não deve fechar ao clicar dentro do modal container', async () => {
        const onCloseMock = vi.fn();
        render(<ConfirmModal isOpen={true} onClose={onCloseMock} title="Container Test" message="M" />);

        // Clicar no título (dentro do container)
        await userEvent.click(screen.getByText('Container Test'));

        expect(onCloseMock).not.toHaveBeenCalled();
    });
});
