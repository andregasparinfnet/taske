import { useState, useEffect } from 'react';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { Calendar, Briefcase, Users, DollarSign, Gavel, BookOpen, MoreHorizontal, AlertTriangle } from 'lucide-react';
import axios from 'axios';

const KanbanView = ({ compromissos, onUpdate }) => {
    const [columns, setColumns] = useState({
        PENDENTE: { id: 'PENDENTE', title: 'A Fazer', color: 'blue' },
        EM_ANDAMENTO: { id: 'EM_ANDAMENTO', title: 'Em Progresso', color: 'orange' },
        CONCLUIDO: { id: 'CONCLUIDO', title: 'Concluído', color: 'green' }
    });



    const onDragEnd = async (result) => {
        const { destination, source, draggableId } = result;

        if (!destination) return;
        if (destination.droppableId === source.droppableId && destination.index === source.index) return;

        // Optimistic UI update
        const newStatus = destination.droppableId;
        const item = compromissos.find(c => c.id.toString() === draggableId);

        // Call parent updater immediately (optimistic)
        onUpdate({ ...item, status: newStatus });

        // Call API
        try {
            await axios.put(`http://localhost:8080/api/compromissos/${draggableId}`, {
                ...item,
                status: newStatus
            });
        } catch (error) {
            console.error("Erro ao mover card", error);
            // Rollback logic could go here
        }
    };

    const getItemsByStatus = (status) => compromissos.filter(c => (c.status || 'PENDENTE') === status);

    return (
        <div className="kanban-board" style={{ display: 'flex', gap: '1.5rem', overflowX: 'auto', paddingBottom: '1rem', height: '100%' }}>
            <DragDropContext onDragEnd={onDragEnd}>
                {Object.values(columns).map(column => (
                    <div key={column.id} className="kanban-column" style={{
                        flex: '0 0 300px',
                        background: column.color === 'blue' ? 'rgba(59, 130, 246, 0.05)' : column.color === 'orange' ? 'rgba(245, 158, 11, 0.05)' : 'rgba(16, 185, 129, 0.05)',
                        borderRadius: '16px',
                        padding: '1rem',
                        display: 'flex',
                        flexDirection: 'column',
                        border: '1px solid rgba(0,0,0,0.02)'
                    }}>
                        <h3 style={{
                            marginBottom: '1rem',
                            fontSize: '0.9rem',
                            textTransform: 'uppercase',
                            letterSpacing: '1px',
                            color: 'var(--text-secondary)',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.5rem'
                        }}>
                            <span style={{ width: 8, height: 8, borderRadius: '50%', background: column.color === 'blue' ? '#3B82F6' : column.color === 'orange' ? '#F59E0B' : '#10B981' }}></span>
                            {column.title}
                            <span style={{ marginLeft: 'auto', background: '#e5e7eb', padding: '2px 8px', borderRadius: '10px', fontSize: '0.75rem' }}>
                                {getItemsByStatus(column.id).length}
                            </span>
                        </h3>

                        <Droppable droppableId={column.id}>
                            {(provided, snapshot) => (
                                <div
                                    {...provided.droppableProps}
                                    ref={provided.innerRef}
                                    style={{
                                        flex: 1,
                                        background: snapshot.isDraggingOver ? 'rgba(0,0,0,0.02)' : 'transparent',
                                        transition: 'background 0.2s',
                                        borderRadius: '8px'
                                    }}
                                >
                                    {getItemsByStatus(column.id).map((item, index) => (
                                        <Draggable key={item.id} draggableId={item.id.toString()} index={index}>
                                            {(provided, snapshot) => (
                                                <div
                                                    ref={provided.innerRef}
                                                    {...provided.draggableProps}
                                                    {...provided.dragHandleProps}
                                                    style={{
                                                        userSelect: 'none',
                                                        padding: '1rem',
                                                        marginBottom: '0.75rem',
                                                        background: 'white',
                                                        borderRadius: '12px',
                                                        border: item.urgente ? '1px solid var(--danger)' : '1px solid var(--border)',
                                                        boxShadow: snapshot.isDragging ? '0 10px 20px rgba(0,0,0,0.1)' : '0 1px 3px rgba(0,0,0,0.05)',
                                                        transform: snapshot.isDragging ? 'scale(1.02) rotate(1deg)' : 'scale(1)',
                                                        ...provided.draggableProps.style
                                                    }}
                                                >
                                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                                            <span className={`tag ${item.tipo}`} style={{ fontSize: '0.65rem', padding: '2px 8px' }}>{item.tipo}</span>
                                                            {item.urgente && <AlertTriangle size={14} color="var(--danger)" fill="var(--danger-bg)" />}
                                                        </div>
                                                    </div>
                                                    <h4 style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.25rem' }}>{item.titulo}</h4>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                                                        <Calendar size={12} />
                                                        {new Date(item.dataHora).toLocaleDateString('pt-BR')}
                                                    </div>
                                                    {item.valor > 0 && (
                                                        <div style={{ marginTop: '0.5rem', fontWeight: 600, color: 'var(--success)', fontSize: '0.85rem' }}>
                                                            R$ {item.valor.toFixed(2)}
                                                        </div>
                                                    )}
                                                </div>
                                            )}
                                        </Draggable>
                                    ))}
                                    {provided.placeholder}
                                </div>
                            )}
                        </Droppable>
                    </div>
                ))}
            </DragDropContext>
        </div>
    );
};

export default KanbanView;
