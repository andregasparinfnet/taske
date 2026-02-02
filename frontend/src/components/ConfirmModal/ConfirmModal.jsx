import React from 'react';
import { AlertTriangle, X } from 'lucide-react';
import './ConfirmModal.css';

const ConfirmModal = ({ isOpen, onClose, onConfirm, title, message }) => {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-container" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <div className="modal-icon-wrapper">
                        <AlertTriangle size={32} />
                    </div>
                    <h3 className="modal-title">{title}</h3>
                    <p className="modal-message">{message}</p>
                </div>
                <div className="modal-actions">
                    <button className="modal-btn modal-btn-cancel" onClick={onClose}>
                        Cancelar
                    </button>
                    <button className="modal-btn modal-btn-confirm" onClick={onConfirm}>
                        Sim, excluir
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmModal;
