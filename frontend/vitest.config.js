import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: './src/test/setup.js',
        coverage: {
            provider: 'v8',
            reporter: ['text', 'html'],
            thresholds: {
                statements: 99,
                branches: 99,
                functions: 99,
                lines: 99
            },
            exclude: [
                'node_modules/',
                'src/test/',
                'src/main.jsx',
                '**/*.test.jsx',
                '*.config.js',
                '*.config.ts'
            ]
        },
        include: ['src/**/*.{test,spec}.{js,jsx}']
    }
});
