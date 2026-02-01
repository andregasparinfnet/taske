import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock localStorage
const localStorageMock = {
    getItem: vi.fn(),
    setItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// Mock window.confirm
window.confirm = vi.fn(() => true);

vi.mock('axios', () => {
    const mockAxios = {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        patch: vi.fn(),
        delete: vi.fn(),
        create: vi.fn(function () { return this; }),
        interceptors: {
            request: { use: vi.fn(), eject: vi.fn() },
            response: { use: vi.fn(), eject: vi.fn() }
        },
        defaults: {
            headers: {
                common: {}
            }
        }
    };
    return {
        default: mockAxios
    };
});

