import { useEffect } from 'react';
import { CheckCircle, AlertCircle, X } from 'lucide-react';
import './Toast.css';

// duration: ms (se 0, não fecha automaticamente)
const Toast = ({ message, type = 'success', onClose, duration }) => {
    useEffect(() => {
        // Definir duração efetiva: erros mais persistentes
        const effectiveDuration =
            typeof duration === 'number'
                ? duration
                : type === 'error'
                    ? 8000 // 8s para erro
                    : 5000; // 5s para sucesso/info

        if (effectiveDuration === 0) return; // sem auto close
        const timer = setTimeout(() => {
            if (typeof onClose === 'function') onClose();
        }, effectiveDuration);
        return () => clearTimeout(timer);
    }, [onClose, type, duration]);

    return (
        <div className={`toast toast-${type}`}>
            {type === 'success' ? <CheckCircle size={24} /> : <AlertCircle size={24} />}
            <span className="toast-message">{message}</span>
            <button onClick={onClose} className="toast-close" aria-label="Fechar"><X size={18} /></button>
        </div>
    );
};

export default Toast;
