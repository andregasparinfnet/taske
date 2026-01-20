import { useState, useEffect } from 'react';
import axios from 'axios';
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

function App() {
  const [compromissos, setCompromissos] = useState([]);
  const [formData, setFormData] = useState({
    titulo: '',
    dataHora: '',
    tipo: 'PERICIA'
  });
  const [erro, setErro] = useState('');

  // Carregar dados ao iniciar
  useEffect(() => {
    carregarCompromissos();
  }, []);

  const carregarCompromissos = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/compromissos');
      setCompromissos(response.data);
    } catch (error) {
      console.error("Erro ao carregar compromissos", error);
    }
  };

  // Stats calculation based on real data
  const stats = {
    total: compromissos.length,
    pericias: compromissos.filter(a => a.tipo === 'PERICIA').length,
    proximos: compromissos.filter(a => new Date(a.dataHora) > new Date()).length
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErro('');

    try {
      await axios.post('http://localhost:8080/api/compromissos', formData);
      setFormData({ titulo: '', dataHora: '', tipo: 'PERICIA' }); // Reset form
      carregarCompromissos(); // Reload list
    } catch (error) {
      console.error("Erro ao salvar", error);
      if (error.response && error.response.status === 400) {
        setErro("Preencha todos os campos obrigatórios corretamente.");
      } else {
        setErro("Ocorreu um erro ao salvar o compromisso.");
      }
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Tem certeza que deseja excluir?")) return;
    try {
      await axios.delete(`http://localhost:8080/api/compromissos/${id}`);
      carregarCompromissos();
    } catch (error) {
      console.error("Erro ao excluir", error);
    }
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
        {erro && <div className="error-banner" style={{ background: '#FEE2E2', color: '#B91C1C', padding: '1rem', borderRadius: '8px', marginBottom: '1rem', border: '1px solid #F87171' }}>{erro}</div>}

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
                  name="titulo"
                  placeholder="Ex: Entrega de Laudo..."
                  value={formData.titulo}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Data e Hora</label>
                  <input
                    type="datetime-local"
                    name="dataHora"
                    value={formData.dataHora}
                    onChange={handleInputChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Tipo</label>
                  <select name="tipo" value={formData.tipo} onChange={handleInputChange}>
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
              <button className="btn-ghost" onClick={carregarCompromissos}>Atualizar</button>
            </div>
            <div className="list-container">
              {compromissos.length === 0 ? (
                <div className="empty-state">Nenhum compromisso agendado.</div>
              ) : (
                compromissos.map((item) => (
                  <div key={item.id} className="list-item">
                    <div className="item-icon">
                      {getTypeIcon(item.tipo)}
                    </div>
                    <div className="item-content">
                      <h3>{item.titulo}</h3>
                      <span className="item-date">
                        {new Date(item.dataHora).toLocaleString('pt-BR', {
                          weekday: 'short',
                          day: '2-digit',
                          month: 'long',
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </span>
                    </div>
                    <div className={`badge ${getBadgeClass(item.tipo)}`}>
                      {item.tipo}
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
