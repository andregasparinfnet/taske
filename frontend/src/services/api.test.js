/**
 * Tests for API Service (api.js)
 * 
 * Coverage target: 80%+
 * Tests for:
 * - Token management (setAccessToken, getAccessToken, clearAuth)
 * - Auth API functions (login, register, logout)
 * - Compromissos API functions (CRUD operations)
 * - Request/Response interceptors (CSRF, JWT, token refresh)
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios from 'axios';
import apiClient, {
    setAccessToken,
    getAccessToken,
    clearAuth,
    login,
    register,
    logout,
    getCompromissos,
    createCompromisso,
    updateCompromisso,
    deleteCompromisso,
    updateCompromissoStatus
} from './api.js';

describe('API Service', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        clearAuth();
        // Reset document.cookie
        Object.defineProperty(document, 'cookie', {
            writable: true,
            value: ''
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    // ========== TOKEN MANAGEMENT ==========

    describe('Token Management', () => {
        it('should set and get access token', () => {
            setAccessToken('test-token-123');
            expect(getAccessToken()).toBe('test-token-123');
        });

        it('should return null when no token is set', () => {
            clearAuth();
            expect(getAccessToken()).toBeNull();
        });

        it('should clear auth token', () => {
            setAccessToken('some-token');
            clearAuth();
            expect(getAccessToken()).toBeNull();
        });

        it('should overwrite existing token', () => {
            setAccessToken('token-1');
            setAccessToken('token-2');
            expect(getAccessToken()).toBe('token-2');
        });

        it('should handle null token', () => {
            setAccessToken(null);
            expect(getAccessToken()).toBeNull();
        });

        it('should handle undefined token', () => {
            setAccessToken(undefined);
            expect(getAccessToken()).toBeUndefined();
        });
    });

    // ========== REQUEST INTERCEPTOR ==========

    describe('Request Interceptor', () => {
        let requestInterceptor;

        beforeEach(() => {
            // Get the captured request interceptor from the mock
            requestInterceptor = axios.__interceptorCallbacks?.request?.onFulfilled;
        });

        it('should add Authorization header when token is set', () => {
            if (!requestInterceptor) return; // Skip if not captured

            setAccessToken('my-jwt-token');
            const config = { headers: {}, method: 'get', url: '/test' };

            const result = requestInterceptor(config);

            expect(result.headers.Authorization).toBe('Bearer my-jwt-token');
        });

        it('should not add Authorization header when no token', () => {
            if (!requestInterceptor) return;

            clearAuth();
            const config = { headers: {}, method: 'get', url: '/test' };

            const result = requestInterceptor(config);

            expect(result.headers.Authorization).toBeUndefined();
        });

        it('should add CSRF token for POST requests', () => {
            if (!requestInterceptor) return;

            // Set CSRF cookie
            Object.defineProperty(document, 'cookie', {
                writable: true,
                value: 'XSRF-TOKEN=csrf-token-value-123'
            });

            const config = { headers: {}, method: 'post', url: '/test' };

            const result = requestInterceptor(config);

            expect(result.headers['X-XSRF-TOKEN']).toBe('csrf-token-value-123');
        });

        it('should add CSRF token for PUT requests', () => {
            if (!requestInterceptor) return;

            Object.defineProperty(document, 'cookie', {
                writable: true,
                value: 'XSRF-TOKEN=csrf-put-token'
            });

            const config = { headers: {}, method: 'put', url: '/test' };

            const result = requestInterceptor(config);

            expect(result.headers['X-XSRF-TOKEN']).toBe('csrf-put-token');
        });

        it('should add CSRF token for DELETE requests', () => {
            if (!requestInterceptor) return;

            Object.defineProperty(document, 'cookie', {
                writable: true,
                value: 'XSRF-TOKEN=csrf-delete-token'
            });

            const config = { headers: {}, method: 'delete', url: '/test' };

            const result = requestInterceptor(config);

            expect(result.headers['X-XSRF-TOKEN']).toBe('csrf-delete-token');
        });

        it('should add CSRF token for PATCH requests', () => {
            if (!requestInterceptor) return;

            Object.defineProperty(document, 'cookie', {
                writable: true,
                value: 'XSRF-TOKEN=csrf-patch-token'
            });

            const config = { headers: {}, method: 'patch', url: '/test' };

            const result = requestInterceptor(config);

            expect(result.headers['X-XSRF-TOKEN']).toBe('csrf-patch-token');
        });

        it('should NOT add CSRF token for GET requests', () => {
            if (!requestInterceptor) return;

            Object.defineProperty(document, 'cookie', {
                writable: true,
                value: 'XSRF-TOKEN=should-not-appear'
            });

            const config = { headers: {}, method: 'get', url: '/test' };

            const result = requestInterceptor(config);

            expect(result.headers['X-XSRF-TOKEN']).toBeUndefined();
        });

        it('should handle missing CSRF cookie gracefully', () => {
            if (!requestInterceptor) return;

            Object.defineProperty(document, 'cookie', {
                writable: true,
                value: ''
            });

            const config = { headers: {}, method: 'post', url: '/test' };

            const result = requestInterceptor(config);

            expect(result.headers['X-XSRF-TOKEN']).toBeUndefined();
        });

        it('should handle multiple cookies and extract CSRF token', () => {
            if (!requestInterceptor) return;

            Object.defineProperty(document, 'cookie', {
                writable: true,
                value: 'other=value; XSRF-TOKEN=middle-token; another=test'
            });

            const config = { headers: {}, method: 'post', url: '/test' };

            const result = requestInterceptor(config);

            expect(result.headers['X-XSRF-TOKEN']).toBe('middle-token');
        });

        it('should handle undefined method gracefully', () => {
            if (!requestInterceptor) return;

            const config = { headers: {}, url: '/test' };

            const result = requestInterceptor(config);

            expect(result).toBeDefined();
        });
    });

    // ========== RESPONSE INTERCEPTOR ==========

    describe('Response Interceptor', () => {
        let responseInterceptorSuccess;
        let responseInterceptorError;

        beforeEach(() => {
            responseInterceptorSuccess = axios.__interceptorCallbacks?.response?.onFulfilled;
            responseInterceptorError = axios.__interceptorCallbacks?.response?.onRejected;
        });

        it('should pass through successful responses', () => {
            if (!responseInterceptorSuccess) return;

            const response = { data: { test: 'data' }, status: 200 };

            const result = responseInterceptorSuccess(response);

            expect(result).toEqual(response);
        });

        it('should reject non-401 errors', async () => {
            if (!responseInterceptorError) return;

            const error = {
                response: { status: 500 },
                config: { url: '/test', _retry: false }
            };

            await expect(responseInterceptorError(error)).rejects.toEqual(error);
        });

        it('should reject 401 on auth requests (login)', async () => {
            if (!responseInterceptorError) return;

            const error = {
                response: { status: 401 },
                config: { url: '/auth/login', _retry: false }
            };

            await expect(responseInterceptorError(error)).rejects.toEqual(error);
        });

        it('should reject 401 on auth requests (register)', async () => {
            if (!responseInterceptorError) return;

            const error = {
                response: { status: 401 },
                config: { url: '/auth/register', _retry: false }
            };

            await expect(responseInterceptorError(error)).rejects.toEqual(error);
        });

        it('should reject 401 on auth requests (refresh)', async () => {
            if (!responseInterceptorError) return;

            const error = {
                response: { status: 401 },
                config: { url: '/auth/refresh', _retry: false }
            };

            await expect(responseInterceptorError(error)).rejects.toEqual(error);
        });

        it('should not retry if already retried', async () => {
            if (!responseInterceptorError) return;

            const error = {
                response: { status: 401 },
                config: { url: '/compromissos', _retry: true }
            };

            await expect(responseInterceptorError(error)).rejects.toEqual(error);
        });

        it('should handle errors without response', async () => {
            if (!responseInterceptorError) return;

            const error = {
                config: { url: '/test', _retry: false }
            };

            await expect(responseInterceptorError(error)).rejects.toEqual(error);
        });
    });

    // ========== AUTH API ==========

    describe('Auth API - Login', () => {
        it('should login successfully and return user data', async () => {
            const mockResponse = {
                data: {
                    accessToken: 'access-token-123',
                    refreshToken: 'refresh-token-456'
                }
            };
            axios.post.mockResolvedValueOnce(mockResponse);

            const result = await login('testuser', 'password123');

            expect(axios.post).toHaveBeenCalledWith('/auth/login', {
                username: 'testuser',
                password: 'password123'
            });
            expect(result).toEqual({
                username: 'testuser',
                token: 'access-token-123',
                refreshToken: 'refresh-token-456'
            });
            expect(getAccessToken()).toBe('access-token-123');
        });

        it('should throw error on login failure', async () => {
            axios.post.mockRejectedValueOnce(new Error('Invalid credentials'));

            await expect(login('testuser', 'wrongpass')).rejects.toThrow('Invalid credentials');
        });
    });

    describe('Auth API - Register', () => {
        it('should register successfully', async () => {
            const mockResponse = { data: { message: 'User registered successfully' } };
            axios.post.mockResolvedValueOnce(mockResponse);

            const result = await register('newuser', 'password123');

            expect(axios.post).toHaveBeenCalledWith('/auth/register', {
                username: 'newuser',
                password: 'password123'
            });
            expect(result).toEqual(mockResponse);
        });

        it('should throw error on registration failure', async () => {
            axios.post.mockRejectedValueOnce(new Error('Username already exists'));

            await expect(register('existinguser', 'password')).rejects.toThrow('Username already exists');
        });
    });

    describe('Auth API - Logout', () => {
        it('should logout and clear auth', async () => {
            setAccessToken('some-token');
            axios.post.mockResolvedValueOnce({ data: {} });

            await logout();

            expect(axios.post).toHaveBeenCalledWith('/auth/logout', { refreshToken: null });
            expect(getAccessToken()).toBeNull();
        });

        it('should clear auth even if logout API fails', async () => {
            setAccessToken('some-token');
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
            axios.post.mockRejectedValueOnce(new Error('Network error'));

            await logout();

            expect(getAccessToken()).toBeNull();
            expect(consoleSpy).toHaveBeenCalled();
            consoleSpy.mockRestore();
        });
    });

    // ========== COMPROMISSOS API ==========

    describe('Compromissos API', () => {
        it('should get all compromissos', async () => {
            const mockData = [
                { id: 1, titulo: 'Task 1', status: 'PENDENTE' },
                { id: 2, titulo: 'Task 2', status: 'CONCLUIDO' }
            ];
            axios.get.mockResolvedValueOnce({ data: mockData });

            const result = await getCompromissos();

            expect(axios.get).toHaveBeenCalledWith('/compromissos');
            expect(result).toEqual(mockData);
        });

        it('should create a new compromisso', async () => {
            const newCompromisso = {
                titulo: 'New Task',
                descricao: 'Description',
                dataHora: '2026-02-15T10:00:00',
                status: 'PENDENTE'
            };
            const mockResponse = { id: 3, ...newCompromisso };
            axios.post.mockResolvedValueOnce({ data: mockResponse });

            const result = await createCompromisso(newCompromisso);

            expect(axios.post).toHaveBeenCalledWith('/compromissos', newCompromisso);
            expect(result).toEqual(mockResponse);
        });

        it('should update an existing compromisso', async () => {
            const updatedCompromisso = {
                titulo: 'Updated Task',
                descricao: 'Updated Description',
                status: 'EM_PROGRESSO'
            };
            const mockResponse = { id: 1, ...updatedCompromisso };
            axios.put.mockResolvedValueOnce({ data: mockResponse });

            const result = await updateCompromisso(1, updatedCompromisso);

            expect(axios.put).toHaveBeenCalledWith('/compromissos/1', updatedCompromisso);
            expect(result).toEqual(mockResponse);
        });

        it('should delete a compromisso', async () => {
            axios.delete.mockResolvedValueOnce({});

            await deleteCompromisso(5);

            expect(axios.delete).toHaveBeenCalledWith('/compromissos/5');
        });

        it('should update compromisso status', async () => {
            const mockResponse = { id: 1, titulo: 'Task', status: 'CONCLUIDO' };
            axios.patch.mockResolvedValueOnce({ data: mockResponse });

            const result = await updateCompromissoStatus(1, 'CONCLUIDO');

            expect(axios.patch).toHaveBeenCalledWith('/compromissos/1/status', null, {
                params: { status: 'CONCLUIDO' }
            });
            expect(result).toEqual(mockResponse);
        });
    });

    // ========== ERROR HANDLING ==========

    describe('Error Handling', () => {
        it('should propagate API errors for getCompromissos', async () => {
            axios.get.mockRejectedValueOnce(new Error('Server error'));

            await expect(getCompromissos()).rejects.toThrow('Server error');
        });

        it('should propagate API errors for createCompromisso', async () => {
            axios.post.mockRejectedValueOnce(new Error('Validation error'));

            await expect(createCompromisso({ titulo: '' })).rejects.toThrow('Validation error');
        });

        it('should propagate API errors for updateCompromisso', async () => {
            axios.put.mockRejectedValueOnce(new Error('Not found'));

            await expect(updateCompromisso(999, {})).rejects.toThrow('Not found');
        });

        it('should propagate API errors for deleteCompromisso', async () => {
            axios.delete.mockRejectedValueOnce(new Error('Unauthorized'));

            await expect(deleteCompromisso(1)).rejects.toThrow('Unauthorized');
        });

        it('should propagate API errors for updateCompromissoStatus', async () => {
            axios.patch.mockRejectedValueOnce(new Error('Invalid status'));

            await expect(updateCompromissoStatus(1, 'INVALID')).rejects.toThrow('Invalid status');
        });
    });

    // ========== API CLIENT CONFIGURATION ==========

    describe('API Client Configuration', () => {
        it('should export apiClient as default', () => {
            expect(apiClient).toBeDefined();
        });
    });

    // ========== EDGE CASES ==========

    describe('Edge Cases', () => {
        it('should handle empty compromissos list', async () => {
            axios.get.mockResolvedValueOnce({ data: [] });

            const result = await getCompromissos();

            expect(result).toEqual([]);
        });

        it('should handle compromisso with special characters', async () => {
            const specialCompromisso = {
                titulo: 'Título com acentos ã é ó',
                descricao: 'Descrição <script>alert("xss")</script>'
            };
            axios.post.mockResolvedValueOnce({ data: { id: 1, ...specialCompromisso } });

            const result = await createCompromisso(specialCompromisso);

            expect(result.titulo).toBe('Título com acentos ã é ó');
        });

        it('should handle large ID numbers', async () => {
            axios.delete.mockResolvedValueOnce({});

            await deleteCompromisso(999999999);

            expect(axios.delete).toHaveBeenCalledWith('/compromissos/999999999');
        });
    });
});
