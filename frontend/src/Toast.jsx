import { useEffect } from 'react';
import { CheckCircle, AlertCircle, X } from 'lucide-react';
import './Toast.css';

const Toast = ({ message, type = 'success', onClose }) => {
    useEffect(() => {
        const timer = setTimeout(() => {
            onClose();
        }, 3000); // 3 seconds
        return () => clearTimeout(timer);
    }, [onClose]);

    return (
        <div className={`toast toast-${type}`}>
            {type === 'success' ? <CheckCircle size={24} /> : <AlertCircle size={24} />}
            <span className="toast-message">{message}</span>
            <button onClick={onClose} className="toast-close" aria-label="Fechar"><X size={18} /></button>
        </div>
    );
};

export default Toast;
