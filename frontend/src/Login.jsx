import { useState } from 'react';
import axios from 'axios';
import { User, Lock, LogIn, UserPlus, CheckCircle, AlertCircle } from 'lucide-react';
import './Login.css';

function Login({ onLogin }) {
    const [isRegistering, setIsRegistering] = useState(false);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        if (isRegistering) {
            // REGISTRATION FLOW
            try {
                await axios.post('http://localhost:8080/api/auth/register', {
                    username,
                    password
                });
                setSuccess('Conta criada com sucesso! Faça login agora.');
                setIsRegistering(false); // Switch back to login
                setPassword(''); // Clear password for security
            } catch (err) {
                console.error("Registration failed", err);
                if (err.response && err.response.data) {
                    setError(typeof err.response.data === 'string' ? err.response.data : 'Erro ao cadastrar.');
                } else {
                    setError('Erro ao criar conta. Tente novamente.');
                }
            } finally {
                setLoading(false);
            }
        } else {
            // LOGIN FLOW
            try {
                const response = await axios.post('http://localhost:8080/api/auth/login', {
                    username,
                    password
                });

                // Extract Token
                const { accessToken } = response.data;
                const authData = { username, token: accessToken };

                onLogin(authData);
            } catch (err) {
                console.error("Login failed", err);
                setError('Credenciais inválidas. Verifique usuário e senha.');
            } finally {
                setLoading(false);
            }
        }
    };

    const toggleMode = () => {
        setIsRegistering(!isRegistering);
        setError('');
        setSuccess('');
        setUsername('');
        setPassword('');
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <div className="login-header">
                    <div className="login-logo">
                        {isRegistering ? <UserPlus size={32} color="white" /> : <User size={32} color="white" />}
                    </div>
                    <h1>{isRegistering ? 'Criar Conta' : 'Bem-vindo ao LifeOS'}</h1>
                    <p>{isRegistering ? 'Preencha os dados abaixo' : 'Entre para gerenciar seus compromissos'}</p>
                </div>

                <form onSubmit={handleSubmit} className="login-form">
                    <div className="form-group">
                        <label>{isRegistering ? 'Escolha um Usuário' : 'Usuário / Login'}</label>
                        <div className="input-with-icon">
                            <User size={20} className="input-icon" />
                            <input
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder={isRegistering ? 'Ex: ana.silva' : 'Digite seu usuário'}
                                required
                                minLength={3}
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label>{isRegistering ? 'Crie uma Senha' : 'Senha'}</label>
                        <div className="input-with-icon">
                            <Lock size={20} className="input-icon" />
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder={isRegistering ? 'Mínimo de 6 caracteres' : 'Sua senha'}
                                required
                                minLength={6}
                            />
                        </div>
                    </div>

                    {error && (
                        <div className="message-banner error">
                            <AlertCircle size={16} />
                            <span>{error}</span>
                        </div>
                    )}

                    {success && (
                        <div className="message-banner success">
                            <CheckCircle size={16} />
                            <span>{success}</span>
                        </div>
                    )}

                    <button type="submit" className="login-btn" disabled={loading}>
                        {loading ? 'Processando...' : (
                            <>
                                {isRegistering ? <UserPlus size={20} /> : <LogIn size={20} />}
                                <span>{isRegistering ? 'Cadastrar' : 'Entrar'}</span>
                            </>
                        )}
                    </button>
                </form>

                <div className="login-footer">
                    <button type="button" className="toggle-btn" onClick={toggleMode}>
                        {isRegistering
                            ? 'Já tem uma conta? Faça login'
                            : 'Não tem conta? Crie uma agora'}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Login;
