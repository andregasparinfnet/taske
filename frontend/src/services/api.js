/**
 * SEC-005: API Service with Auto-Refresh Token Logic
 * 
 * Features:
 * - Axios interceptors for automatic token refresh
 * - httpOnly cookie support (backend sets cookies)
 * - Access token stored in memory (not localStorage for security)
 * - Automatic retry of failed requests after token refresh
 */

import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const API_BASE_URL = `${API_URL}/api`;

// In-memory access token (SEC-005: not in localStorage)
let accessToken = null;

/**
 * SEC-001: CSRF Token Helper
 * Reads the CSRF token from the XSRF-TOKEN cookie set by the backend
 * @returns {string|null} CSRF token or null if not found
 */
function getCsrfToken() {
    const name = 'XSRF-TOKEN';
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);

    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }

    return null;
}

// Create axios instance
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true, // SEC-005: Enable httpOnly cookies
    headers: {
        'Content-Type': 'application/json'
    }
});

// Request interceptor: Attach access token + CSRF token
apiClient.interceptors.request.use(
    (config) => {
        // SEC-005: Attach JWT access token
        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }

        // SEC-001: CSRF Protection - Double Submit Cookie Pattern
        // For state-changing requests (POST, PUT, DELETE, PATCH), send CSRF token from cookie
        const method = config.method?.toLowerCase();
        if (['post', 'put', 'delete', 'patch'].includes(method)) {
            const csrfToken = getCsrfToken();
            if (csrfToken) {
                config.headers['X-XSRF-TOKEN'] = csrfToken;
            }
        }

        return config;
    },
    (error) => Promise.reject(error)
);


// Response interceptor: Handle 401 and auto-refresh
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // SEC-005: If 401 and not already retrying, and NOT a login/register request
        // We don't want to try to refresh token if we are already trying to authenticate
        const isAuthRequest = originalRequest.url.includes('/auth/login') ||
            originalRequest.url.includes('/auth/register') ||
            originalRequest.url.includes('/auth/refresh');

        if (error.response?.status === 401 && !originalRequest._retry && !isAuthRequest) {
            originalRequest._retry = true;

            try {
                // Refresh token (refresh token in httpOnly cookie automatically sent)
                const refreshResponse = await axios.post(
                    `${API_BASE_URL}/auth/refresh`,
                    {},
                    { withCredentials: true } // Send httpOnly cookie
                );

                const newAccessToken = refreshResponse.data.accessToken;
                setAccessToken(newAccessToken);

                // Retry original request with new token
                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                return apiClient(originalRequest);
            } catch (refreshError) {
                // Refresh failed - user needs to login
                console.error('Token refresh failed:', refreshError);
                clearAuth();
                window.location.href = '/'; // Redirect to login
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

// ====== TOKEN MANAGEMENT ======

export function setAccessToken(token) {
    accessToken = token;
}

export function getAccessToken() {
    return accessToken;
}

export function clearAuth() {
    accessToken = null;
}

// ====== AUTH API ======

export async function login(username, password) {
    const response = await apiClient.post('/auth/login', { username, password });
    const { accessToken: token, refreshToken } = response.data;

    // SEC-005: Store access token in memory only
    setAccessToken(token);

    return { username, token, refreshToken };
}

export async function register(username, password) {
    return await apiClient.post('/auth/register', { username, password });
}

export async function logout() {
    try {
        const refreshToken = null; // Will be sent via httpOnly cookie
        await apiClient.post('/auth/logout', { refreshToken });
    } catch (error) {
        console.error('Logout error:', error);
    } finally {
        clearAuth();
    }
}

// ====== COMPROMISSOS API ======

export async function getCompromissos() {
    const response = await apiClient.get('/compromissos');
    return response.data;
}

export async function createCompromisso(compromisso) {
    const response = await apiClient.post('/compromissos', compromisso);
    return response.data;
}

export async function updateCompromisso(id, compromisso) {
    const response = await apiClient.put(`/compromissos/${id}`, compromisso);
    return response.data;
}

export async function deleteCompromisso(id) {
    await apiClient.delete(`/compromissos/${id}`);
}

export async function updateCompromissoStatus(id, status) {
    const response = await apiClient.patch(`/compromissos/${id}/status`, null, {
        params: { status }
    });
    return response.data;
}

export default apiClient;
