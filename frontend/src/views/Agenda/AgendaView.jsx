import { useState } from 'react';
import { Calendar, Clock, MapPin, AlertTriangle, Filter, Layers, Download } from 'lucide-react';
import apiClient from '../../services/api';

const AgendaView = ({ compromissos }) => {
    const [activeFilter, setActiveFilter] = useState('all');
    const [exporting, setExporting] = useState(false);

    const handleExport = async () => {
        try {
            setExporting(true);
            const res = await apiClient.get('/compromissos/export', {
                params: { format: 'csv' },
                responseType: 'blob'
            });
            const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'compromissos.csv';
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        } catch (e) {
            console.error('Export error:', e);
            alert('Falha ao exportar compromissos. Tente novamente.');
        } finally {
            setExporting(false);
        }
    };

    // Helper functions for dates
    const getStartOfDay = (date) => {
        const d = new Date(date);
        d.setHours(0, 0, 0, 0);
        return d;
    };

    const today = getStartOfDay(new Date());
    const next7Days = new Date(today);
    next7Days.setDate(today.getDate() + 7);
    const next15Days = new Date(today);
    next15Days.setDate(today.getDate() + 15);

    // Calculate counts
    const getCount = (range) => {
        return compromissos.filter(item => {
            const itemDate = getStartOfDay(item.dataHora);
            const isToday = itemDate.getTime() === today.getTime();
            const isIn7Days = itemDate >= today && itemDate <= next7Days;
            const isIn15Days = itemDate >= today && itemDate <= next15Days;

            if (range === 'today') return isToday;
            if (range === 'next7') return isIn7Days;
            return isIn15Days; // range === 'next15' is the only other option called
        }).length;
    };

    const counts = {
        today: getCount('today'),
        next7: getCount('next7'),
        next15: getCount('next15'),
        total: compromissos.length
    };

    // Filter Logic
    const filteredCompromissos = compromissos.filter(item => {
        const itemDate = getStartOfDay(item.dataHora);
        if (activeFilter === 'today') return itemDate.getTime() === today.getTime();
        if (activeFilter === 'next7') return itemDate >= today && itemDate <= next7Days;
        if (activeFilter === 'next15') return itemDate >= today && itemDate <= next15Days;
        return true;
    });

    const toggleFilter = (filter) => {
        setActiveFilter(prev => prev === filter ? 'all' : filter);
    };

    // Sort underlying list first
    const sortedList = [...filteredCompromissos].sort((a, b) => new Date(a.dataHora) - new Date(b.dataHora));

    // Re-group sorted
    const finalGrouped = sortedList.reduce((acc, item) => {
        const date = new Date(item.dataHora);
        const dateKey = date.toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });
        const isToday = new Date().toDateString() === date.toDateString();

        const key = isToday ? 'Hoje' : dateKey;

        if (!acc[key]) acc[key] = [];
        acc[key].push(item);
        return acc;
    }, {});

    return (
        <div className="agenda-view" style={{ maxWidth: '800px', margin: '0 auto' }}>

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem' }}>
                <button
                    onClick={handleExport}
                    disabled={exporting}
                    style={{
                        background: 'var(--primary)',
                        color: 'white',
                        border: 'none',
                        padding: '0.6rem 1rem',
                        borderRadius: '8px',
                        fontWeight: 600,
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        cursor: exporting ? 'not-allowed' : 'pointer',
                        opacity: exporting ? 0.7 : 1
                    }}
                >
                    <Download size={18} />
                    {exporting ? 'Exportando...' : 'Exportar CSV'}
                </button>
            </div>
            {/* Filter Buttons (Dashboard Style) */}
            <div className="stats-grid" style={{ marginBottom: '2rem', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))' }}>
                <div
                    className="stat-card"
                    onClick={() => setActiveFilter('all')}
                    style={{
                        cursor: 'pointer',
                        border: activeFilter === 'all' ? '2px solid var(--text-main)' : '1px solid transparent',
                        backgroundColor: activeFilter === 'all' ? 'rgba(0, 0, 0, 0.05)' : 'white',
                        transition: 'all 0.2s ease'
                    }}
                >
                    <div className="stat-icon" style={{ background: '#F3F4F6', color: '#374151' }}><Layers /></div>
                    <div className="stat-info">
                        <h3>Todos</h3>
                        <p>{counts.total}</p>
                    </div>
                </div>
                <div
                    className="stat-card"
                    onClick={() => toggleFilter('today')}
                    style={{
                        cursor: 'pointer',
                        border: activeFilter === 'today' ? '2px solid var(--primary)' : '1px solid transparent',
                        backgroundColor: activeFilter === 'today' ? 'rgba(99, 102, 241, 0.05)' : 'white',
                        transition: 'all 0.2s ease'
                    }}
                >
                    <div className="stat-icon blue"><Calendar /></div>
                    <div className="stat-info">
                        <h3>Hoje</h3>
                        <p>{counts.today}</p>
                    </div>
                </div>

                <div
                    className="stat-card"
                    onClick={() => toggleFilter('next7')}
                    style={{
                        cursor: 'pointer',
                        border: activeFilter === 'next7' ? '2px solid var(--secondary)' : '1px solid transparent',
                        backgroundColor: activeFilter === 'next7' ? 'rgba(16, 185, 129, 0.05)' : 'white',
                        transition: 'all 0.2s ease'
                    }}
                >
                    <div className="stat-icon green"><Clock /></div>
                    <div className="stat-info">
                        <h3>Próximos 7 dias</h3>
                        <p>{counts.next7}</p>
                    </div>
                </div>

                <div
                    className="stat-card"
                    onClick={() => toggleFilter('next15')}
                    style={{
                        cursor: 'pointer',
                        border: activeFilter === 'next15' ? '2px solid #F59E0B' : '1px solid transparent',
                        backgroundColor: activeFilter === 'next15' ? 'rgba(245, 158, 11, 0.05)' : 'white',
                        transition: 'all 0.2s ease'
                    }}
                >
                    <div className="stat-icon" style={{ background: '#FEF3C7', color: '#D97706' }}><MapPin /></div>
                    <div className="stat-info">
                        <h3>Próximos 15 dias</h3>
                        <p>{counts.next15}</p>
                    </div>
                </div>
            </div>

            {Object.keys(finalGrouped).length === 0 && (
                <div className="empty-state" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                    <Filter size={48} style={{ opacity: 0.2, marginBottom: '1rem' }} />
                    <p>Nenhum compromisso encontrado para este filtro.</p>
                    {activeFilter !== 'all' && (
                        <button
                            onClick={() => setActiveFilter('all')}
                            style={{
                                marginTop: '1rem',
                                background: 'none',
                                border: 'none',
                                color: 'var(--primary)',
                                fontWeight: 600,
                                cursor: 'pointer'
                            }}
                        >
                            Limpar filtros
                        </button>
                    )}
                </div>
            )}

            {Object.entries(finalGrouped).map(([date, items]) => (
                <div key={date} className="agenda-day" style={{ marginBottom: '2rem' }}>
                    <h3 style={{
                        fontSize: '1.1rem',
                        fontWeight: 700,
                        color: 'var(--primary)',
                        borderBottom: '2px solid var(--border)',
                        paddingBottom: '0.5rem',
                        marginBottom: '1rem',
                        textTransform: 'capitalize'
                    }}>
                        {date}
                    </h3>

                    <div className="agenda-events" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        {items.map(item => (
                            <div key={item.id} className="agenda-card" style={{
                                display: 'flex',
                                gap: '1rem',
                                background: 'white',
                                padding: '1rem',
                                borderRadius: '12px',
                                border: '1px solid var(--border)',
                                borderLeft: `4px solid ${item.urgente ? 'var(--danger)' : (item.tipo === 'PERICIA' ? 'var(--danger)' : 'var(--primary)')}`
                            }}>
                                <div className="time-column" style={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    alignItems: 'center',
                                    minWidth: '60px',
                                    fontWeight: 600,
                                    color: 'var(--text-main)'
                                }}>
                                    <span>{new Date(item.dataHora).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}</span>
                                </div>

                                <div className="event-details" style={{ flex: 1 }}>
                                    <h4 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.25rem' }}>{item.titulo}</h4>
                                    <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>{item.descricao || 'Sem descrição'}</p>

                                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                                        <span className={`tag ${item.tipo}`}>{item.tipo}</span>
                                        {item.urgente && (
                                            <span style={{
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: '4px',
                                                fontSize: '0.7rem',
                                                color: 'var(--danger)',
                                                background: 'rgba(239, 68, 68, 0.1)',
                                                padding: '2px 8px',
                                                borderRadius: '20px',
                                                fontWeight: '600'
                                            }}>
                                                <AlertTriangle size={12} /> URGENTE
                                            </span>
                                        )}
                                        {item.valor > 0 && <span className="tag FINANCEIRO">R$ {item.valor}</span>}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            ))}
        </div>
    );
};

export default AgendaView;
