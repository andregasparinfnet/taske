import { Calendar, Clock, MapPin } from 'lucide-react';

const AgendaView = ({ compromissos }) => {
    // Group by date
    const grouped = compromissos.reduce((acc, item) => {
        const dateKey = new Date(item.dataHora).toLocaleDateString('pt-BR', {
            weekday: 'long',
            day: 'numeric',
            month: 'long'
        });
        if (!acc[dateKey]) acc[dateKey] = [];
        acc[dateKey].push(item);
        return acc;
    }, {});

    // Sort dates
    const sortedDates = Object.keys(grouped).sort((a, b) => {
        // Need raw date for sorting, this is a bit rough but works for display logic if we assume listing order.
        // Better to sort compromissos first.
        return 0;
    });

    // Sort underlying list first
    const sortedList = [...compromissos].sort((a, b) => new Date(a.dataHora) - new Date(b.dataHora));

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
                                borderLeft: `4px solid ${item.tipo === 'PERICIA' ? 'var(--danger)' : 'var(--primary)'}`
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

                                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                                        <span className={`tag ${item.tipo}`}>{item.tipo}</span>
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
