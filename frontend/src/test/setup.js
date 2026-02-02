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
    // Store interceptor callbacks so they can be tested
    const interceptorCallbacks = {
        request: { onFulfilled: null, onRejected: null },
        response: { onFulfilled: null, onRejected: null }
    };

    const mockAxios = {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        patch: vi.fn(),
        delete: vi.fn(),
        create: vi.fn(function () { return this; }),
        interceptors: {
            request: {
                use: vi.fn((onFulfilled, onRejected) => {
                    interceptorCallbacks.request.onFulfilled = onFulfilled;
                    interceptorCallbacks.request.onRejected = onRejected;
                }),
                eject: vi.fn()
            },
            response: {
                use: vi.fn((onFulfilled, onRejected) => {
                    interceptorCallbacks.response.onFulfilled = onFulfilled;
                    interceptorCallbacks.response.onRejected = onRejected;
                }),
                eject: vi.fn()
            }
        },
        defaults: {
            headers: {
                common: {}
            }
        },
        // Expose interceptor callbacks for testing
        __interceptorCallbacks: interceptorCallbacks
    };
    return {
        default: mockAxios
    };
});

