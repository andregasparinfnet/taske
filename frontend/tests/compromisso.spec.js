import { test, expect } from '@playwright/test';

test.describe('Compromisso CRUD Flow', () => {

    test.beforeEach(async ({ page }) => {
        const timestamp = Date.now() + Math.random();
        const username = `user_${timestamp}`;
        const password = 'Password@123';

        // Register and login
        await page.goto('/');
        await page.click('text=Não tem conta? Crie uma agora');
        await page.fill('[placeholder="Ex: ana.silva"]', username);
        await page.fill('[placeholder="Mínimo de 6 caracteres"]', password);
        await page.click('button:has-text("Cadastrar")');

        await page.fill('[placeholder="Digite seu usuário"]', username);
        await page.fill('[placeholder="Sua senha"]', password);
        await page.click('button:has-text("Entrar")');
        await expect(page.locator('text=Minha Agenda')).toBeVisible();
    });

    test('should create, edit and delete a compromisso', async ({ page }) => {
        await page.click('text=Dashboard');
        await expect(page.locator('text=Visão Geral')).toBeVisible();

        const titulo = `Compromisso E2E ${Date.now()}`;
        await page.fill('input[name="titulo"]', titulo);
        await page.fill('input[name="dataHora"]', '2026-02-15T14:30');
        await page.selectOption('select[name="tipo"]', 'TRABALHO');
        await page.fill('input[name="valor"]', '150.50');
        await page.click('input[name="urgente"]');
        await page.fill('textarea[name="descricao"]', 'Descrição do compromisso E2E');

        await page.click('button:has-text("Salvar Compromisso")');

        // Verify creation
        await expect(page.locator(`text=${titulo}`)).toBeVisible();

        // Edit
        await page.locator('.action-btn:not(.delete)').first().click();
        await expect(page.locator('text=Editar Compromisso')).toBeVisible();

        const updatedTitulo = `${titulo} - EDITADO`;
        await page.fill('input[name="titulo"]', updatedTitulo);
        await page.click('button:has-text("Atualizar")');

        await expect(page.locator('text=Compromisso atualizado com sucesso!')).toBeVisible();
        await expect(page.locator(`text=${updatedTitulo}`)).toBeVisible();

        // Delete
        await page.locator('.action-btn.delete').first().click();
        await expect(page.locator('text=Excluir Compromisso')).toBeVisible();
        await page.click('button:has-text("Sim, excluir")');

        await expect(page.locator('text=Compromisso excluído com sucesso!')).toBeVisible();
        await expect(page.locator(`text=${updatedTitulo}`)).not.toBeVisible();
    });

    test('should cancel editing', async ({ page }) => {
        await page.click('text=Dashboard');

        await page.fill('input[name="titulo"]', 'Item para cancelar');
        await page.fill('input[name="dataHora"]', '2026-02-15T10:00');
        await page.click('button:has-text("Salvar Compromisso")');

        await page.locator('.action-btn:not(.delete)').first().click();
        await expect(page.locator('text=Editar Compromisso')).toBeVisible();

        await page.click('text=Cancelar Edição');
        await expect(page.locator('text=Novo Compromisso')).toBeVisible();
    });
});
