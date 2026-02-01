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

// Mock axios with create method for api.js compatibility
const mockAxiosInstance = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
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

vi.mock('axios', () => ({
    default: {
        ...mockAxiosInstance,
        create: vi.fn(() => mockAxiosInstance),
        post: vi.fn(),
        get: vi.fn()
    }
}));

