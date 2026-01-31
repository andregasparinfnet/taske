import { Calendar, Gavel, CheckCircle, Clock } from 'lucide-react';

const DashboardView = ({ stats }) => {
    return (
        <div className="dashboard-view animate-fade-in">
            {/* KPIs */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon blue"><Calendar /></div>
                    <div className="stat-info">
                        <h3>Total</h3>
                        <p>{stats.total}</p>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon red"><Gavel /></div>
                    <div className="stat-info">
                        <h3>Perícias</h3>
                        <p>{stats.pericias}</p>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon green"><Clock /></div>
                    <div className="stat-info">
                        <h3>Próximos</h3>
                        <p>{stats.proximos}</p>
                    </div>
                </div>
            </div>

            {/* Hero Section / Welcome Widget */}
            <div className="welcome-widget" style={{
                background: 'linear-gradient(135deg, var(--primary) 0%, #4338CA 100%)',
                borderRadius: '24px',
                padding: '2rem',
                color: 'white',
                marginBottom: '2rem',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                boxShadow: '0 10px 25px -5px rgba(79, 70, 229, 0.4)'
            }}>
                <div>
                    <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem' }}>Painel de Controle</h2>
                    <p style={{ opacity: 0.9 }}>Você tem {stats.proximos} atividades agendadas para os próximos dias.</p>
                </div>
                <div style={{ background: 'rgba(255,255,255,0.2)', padding: '1rem', borderRadius: '16px' }}>
                    <CheckCircle size={32} />
                </div>
            </div>
        </div>
    );
};

export default DashboardView;
