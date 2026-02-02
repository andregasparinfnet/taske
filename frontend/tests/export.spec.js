import { test, expect } from '@playwright/test';

test.describe('Export Functionality E2E', () => {

    test.beforeEach(async ({ page }) => {
        const username = `export_user_${Date.now()}`;
        const password = 'Password@123';

        // Register
        await page.goto('/');
        await page.click('text=Não tem conta? Crie uma agora');
        await page.fill('[placeholder="Ex: ana.silva"]', username);
        await page.fill('[placeholder="Mínimo de 6 caracteres"]', password);
        await page.click('button:has-text("Cadastrar")');

        // Login
        await page.fill('[placeholder="Digite seu usuário"]', username);
        await page.fill('[placeholder="Sua senha"]', password);
        await page.click('button:has-text("Entrar")');
        await expect(page.locator('text=Minha Agenda')).toBeVisible();

        // Create a compromisso to ensure there is data to export
        await page.click('text=Dashboard');
        await page.fill('input[name="titulo"]', 'Tarefa para Exportar');
        await page.fill('input[name="dataHora"]', '2026-03-01T10:00');
        await page.selectOption('select[name="tipo"]', 'TRABALHO');
        await page.click('button:has-text("Salvar Compromisso")');
        await expect(page.locator('text=Tarefa para Exportar')).toBeVisible();
    });

    test('should trigger CSV download when clicking export button in Agenda View', async ({ page }) => {
        // Go to Agenda View where the Export button is located
        await page.click('text=Agenda');
        await expect(page.locator('text=Exportar CSV')).toBeVisible();

        // Start waiting for download before clicking
        const downloadPromise = page.waitForEvent('download');

        await page.click('button:has-text("Exportar CSV")');

        const download = await downloadPromise;

        // Wait for the download process to complete
        const path = await download.path();

        // Assertions
        expect(download.suggestedFilename()).toContain('compromissos');
        expect(download.suggestedFilename()).toContain('.csv');
        expect(path).not.toBeNull();

        console.log(`Download successful: ${download.suggestedFilename()}`);
    });
});
