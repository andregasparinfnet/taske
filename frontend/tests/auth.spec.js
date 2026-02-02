import { test, expect } from '@playwright/test';

test.describe.configure({ mode: 'serial' }); // Run tests in this file sequentially

test.describe('Authentication Flow', () => {
    const timestamp = Date.now();
    const username = `auth_user_${timestamp}`;
    const password = 'Password@123';

    test('should register a new user', async ({ page }) => {
        await page.goto('/');
        await page.click('text=Não tem conta? Crie uma agora');
        await page.fill('[placeholder="Ex: ana.silva"]', username);
        await page.fill('[placeholder="Mínimo de 6 caracteres"]', password);
        await page.click('button:has-text("Cadastrar")');
        await expect(page.locator('text=Conta criada com sucesso!')).toBeVisible();
    });

    test('should login successfully', async ({ page }) => {
        await page.goto('/');
        await page.fill('[placeholder="Digite seu usuário"]', username);
        await page.fill('[placeholder="Sua senha"]', password);
        await page.click('button:has-text("Entrar")');
        await expect(page.locator('text=Minha Agenda')).toBeVisible();
    });

    test('should logout successfully', async ({ page }) => {
        await page.goto('/');
        await page.fill('[placeholder="Digite seu usuário"]', username);
        await page.fill('[placeholder="Sua senha"]', password);
        await page.click('button:has-text("Entrar")');
        await expect(page.locator('text=Minha Agenda')).toBeVisible();

        // Click Sair
        await page.click('text=Sair');
        await expect(page.locator('text=Bem-vindo ao LifeOS')).toBeVisible();
    });
});
