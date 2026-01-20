import { useState } from 'react';
import './App.css';
import {
  LayoutDashboard,
  Calendar,
  Plus,
  Trash2,
  Edit2,
  CheckCircle,
  Briefcase,
  Gavel,
  Users,
  DollarSign
} from 'lucide-react';

// Mock de Dados Inicial
const INITIAL_DATA = [
  { id: 1, title: 'Entrega de Laudo - Processo TJ-RJ 8921', date: '2023-10-25T14:00', type: 'PERICIA' },
  { id: 2, title: 'Reunião Escolar - Pedro', date: '2023-10-26T09:00', type: 'FAMILIA' },
  { id: 3, title: 'Revisão Fiscal Trimestral', date: '2023-10-28T10:00', type: 'FINANCEIRO' },
  { id: 4, title: 'Almoço com Sócios', date: '2023-10-27T12:30', type: 'TRABALHO' },
];

function App() {
  const [appointments, setAppointments] = useState(INITIAL_DATA);
  const [formData, setFormData] = useState({
    title: '',
    date: '',
    type: 'PERICIA'
  });

  // Cálculo de Resumo (Stats)
  const stats = {
    total: appointments.length,
    pericias: appointments.filter(a => a.type === 'PERICIA').length,
    proximos: appointments.filter(a => new Date(a.date) > new Date()).length
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.title || !formData.date) return;

    const newAppointment = {
      id: Date.now(),
      ...formData
    };

    setAppointments([newAppointment, ...appointments]);
    setFormData({ title: '', date: '', type: 'PERICIA' }); // Reset form
  };

  const handleDelete = (id) => {
    setAppointments(appointments.filter(a => a.id !== id));
  };

  const getBadgeClass = (type) => {
    switch (type) {
      case 'PERICIA': return 'badge-red';
      case 'TRABALHO': return 'badge-blue';
      case 'FAMILIA': return 'badge-green';
      case 'FINANCEIRO': return 'badge-yellow';
      default: return 'badge-gray';
    }
  };

  const getTypeIcon = (type) => {
    switch (type) {
      case 'PERICIA': return <Gavel size={16} />;
      case 'TRABALHO': return <Briefcase size={16} />;
      case 'FAMILIA': return <Users size={16} />;
      case 'FINANCEIRO': return <DollarSign size={16} />;
      default: return <Calendar size={16} />;
    }
  }

  return (
    <div className="app-container">
      {/* 1. Header */}
      <header className="header">
        <div className="logo-container">
          <div className="logo-icon">
            <LayoutDashboard size={24} color="white" />
          </div>
          <h1>LifeOS</h1>
        </div>
        <nav className="nav-menu">
          <a href="#" className="active">Dashboard</a>
          <a href="#">Agenda</a>
          <a href="#">Financeiro</a>
          <a href="#">Processos</a>
        </nav>
        <div className="user-profile">
          <span>Dr. Perito</span>
          <div className="avatar">P</div>
        </div>
      </header>

      <main className="main-content">
        {/* 2. Cards de Resumo */}
        <section className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon bg-blue"><Calendar size={20} /></div>
            <div className="stat-info">
              <span className="stat-label">Total Compromissos</span>
              <span className="stat-value">{stats.total}</span>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon bg-red"><Gavel size={20} /></div>
            <div className="stat-info">
              <span className="stat-label">Perícias Pendentes</span>
              <span className="stat-value">{stats.pericias}</span>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon bg-green"><CheckCircle size={20} /></div>
            <div className="stat-info">
              <span className="stat-label">Próximos (7 dias)</span>
              <span className="stat-value">{stats.proximos}</span>
            </div>
          </div>
        </section>

        <div className="dashboard-grid">
          {/* 3. Formulário de Cadastro */}
          <section className="card form-section">
            <div className="card-header">
              <h2><Plus size={18} /> Novo Compromisso</h2>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Título</label>
                <input
                  type="text"
                  name="title"
                  placeholder="Ex: Entrega de Laudo..."
                  value={formData.title}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Data e Hora</label>
                  <input
                    type="datetime-local"
                    name="date"
                    value={formData.date}
                    onChange={handleInputChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Tipo</label>
                  <select name="type" value={formData.type} onChange={handleInputChange}>
                    <option value="PERICIA">Perícia</option>
                    <option value="TRABALHO">Trabalho</option>
                    <option value="FAMILIA">Família</option>
                    <option value="FINANCEIRO">Financeiro</option>
                  </select>
                </div>
              </div>

              <button type="submit" className="btn-primary">
                Salvar Compromisso
              </button>
            </form>
          </section>

          {/* 4. Lista de Compromissos */}
          <section className="card list-section">
            <div className="card-header">
              <h2>Próximos Compromissos</h2>
              <button className="btn-ghost">Ver todos</button>
            </div>
            <div className="list-container">
              {appointments.length === 0 ? (
                <div className="empty-state">Nenhum compromisso agendado.</div>
              ) : (
                appointments.map((item) => (
                  <div key={item.id} className="list-item">
                    <div className="item-icon">
                      {getTypeIcon(item.type)}
                    </div>
                    <div className="item-content">
                      <h3>{item.title}</h3>
                      <span className="item-date">
                        {new Date(item.date).toLocaleString('pt-BR', {
                          weekday: 'short',
                          day: '2-digit',
                          month: 'long',
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </span>
                    </div>
                    <div className={`badge ${getBadgeClass(item.type)}`}>
                      {item.type}
                    </div>
                    <div className="item-actions">
                      <button className="btn-icon"><Edit2 size={16} /></button>
                      <button className="btn-icon delete" onClick={() => handleDelete(item.id)}>
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}

export default App;
