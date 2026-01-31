import { useState, useEffect } from 'react';
import axios from 'axios';
import Login from './Login';
import KanbanView from './KanbanView';
import AgendaView from './AgendaView';
import DashboardView from './DashboardView';
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
  DollarSign,
  LogOut,
  ChevronRight,
  Search,
  List
} from 'lucide-react';

function App() {
  const [user, setUser] = useState(null);
  const [compromissos, setCompromissos] = useState([]);
  const [activeTab, setActiveTab] = useState('dashboard'); // dashboard, agenda, kanban
  const [formData, setFormData] = useState({
    titulo: '',
    dataHora: '',
    tipo: 'PERICIA',
    status: 'PENDENTE',
    valor: '',
    descricao: ''
  });
  const [erro, setErro] = useState('');
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    const savedAuth = localStorage.getItem('auth');
    if (savedAuth) {
      const auth = JSON.parse(savedAuth);
      if (auth.token) {
        setUser(auth);
        axios.defaults.headers.common['Authorization'] = `Bearer ${auth.token}`;
        carregarCompromissos();
      }
    }
  }, []);

  const carregarCompromissos = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/compromissos');
      setCompromissos(response.data);
    } catch (error) {
      if (error.response && error.response.status === 403) handleLogout();
    }
  };

  const handleLogin = (auth) => {
    setUser(auth);
    axios.defaults.headers.common['Authorization'] = `Bearer ${auth.token}`;
    localStorage.setItem('auth', JSON.stringify(auth));
    carregarCompromissos();
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('auth');
    setCompromissos([]);
    delete axios.defaults.headers.common['Authorization'];
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErro('');
    try {
      if (editingId) {
        await axios.put(`http://localhost:8080/api/compromissos/${editingId}`, formData);
        setEditingId(null);
      } else {
        await axios.post('http://localhost:8080/api/compromissos', formData);
      }
      setFormData({ titulo: '', dataHora: '', tipo: 'PERICIA', status: 'PENDENTE', valor: '', descricao: '' });
      carregarCompromissos();
    } catch (error) {
      setErro("Erro ao salvar. Verifique os campos.");
    }
  };

  const handleEdit = (item) => {
    setFormData({
      titulo: item.titulo,
      dataHora: item.dataHora,
      tipo: item.tipo,
      status: item.status || 'PENDENTE',
      valor: item.valor || '',
      descricao: item.descricao || ''
    });
    setEditingId(item.id);
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Excluir este item?")) return;
    try {
      await axios.delete(`http://localhost:8080/api/compromissos/${id}`);
      carregarCompromissos();
    } catch (error) { console.error(error); }
  };

  const handleUpdateItem = (updatedItem) => {
    setCompromissos(prev => prev.map(i => i.id === updatedItem.id ? updatedItem : i));
  };

  const getTagIcon = (type) => {
    switch (type) {
      case 'PERICIA': return <Gavel size={18} />;
      case 'TRABALHO': return <Briefcase size={18} />;
      case 'FAMILIA': return <Users size={18} />;
      case 'FINANCEIRO': return <DollarSign size={18} />;
      default: return <Calendar size={18} />;
    }
  };

  const stats = {
    total: compromissos.length,
    pericias: compromissos.filter(a => a.tipo === 'PERICIA').length,
    proximos: compromissos.filter(a => new Date(a.dataHora) > new Date()).length
  };

  if (!user) return <Login onLogin={handleLogin} />;

  return (
    <div className="app-container">

      {/* Sidebar */}
      <nav className="sidebar">
        <div className="logo-container">
          <div className="logo-bg"><LayoutDashboard size={24} /></div>
          <span className="logo-text">LifeOS</span>
        </div>

        <div className="nav-menu">
          <button onClick={() => setActiveTab('dashboard')} className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}>
            <LayoutDashboard size={20} />
            <span className="sidebar-label">Dashboard</span>
          </button>
          <button onClick={() => setActiveTab('agenda')} className={`nav-item ${activeTab === 'agenda' ? 'active' : ''}`}>
            <Calendar size={20} />
            <span className="sidebar-label">Agenda</span>
          </button>
          <button onClick={() => setActiveTab('kanban')} className={`nav-item ${activeTab === 'kanban' ? 'active' : ''}`}>
            <List size={20} />
            <span className="sidebar-label">Quadros</span>
          </button>
        </div>

        <div className="user-profile">
          <button onClick={handleLogout} className="nav-item logout-btn-sidebar">
            <LogOut size={20} />
            <span className="sidebar-label">Sair</span>
          </button>
        </div>
      </nav>

      {/* Main Content */}
      <main className="main-content">
        <header className="mobile-header">
          <div className="logo-bg" style={{ width: 32, height: 32 }}><LayoutDashboard size={18} /></div>
          <h1 style={{ fontSize: '1.2rem', fontWeight: 700 }}>LifeOS</h1>
          <button onClick={handleLogout} style={{ background: 'none', border: 'none', color: 'var(--danger)' }}><LogOut size={20} /></button>
        </header>

        <header className="dashboard-header">
          <div className="dashboard-title">
            <h1>{activeTab === 'dashboard' ? 'Visão Geral' : activeTab === 'agenda' ? 'Minha Agenda' : 'Quadro de Atividades'}</h1>
            <p>Gerencie seus compromissos e tarefas.</p>
          </div>
        </header>

        {/* Dynamic Content Area */}
        {activeTab === 'dashboard' && (
          <>
            <DashboardView stats={stats} />
            <div className="content-grid">
              {/* List */}
              <section className="card list-section">
                <div className="card-header">
                  <h2>Compromissos Recentes</h2>
                </div>
                <div className="list-container">
                  {compromissos.map((item) => (
                    <div key={item.id} className="list-item">
                      <div className="item-icon-box">{getTagIcon(item.tipo)}</div>
                      <div className="item-content">
                        <h3 className="item-title">{item.titulo}</h3>
                        <div className="item-meta">
                          <span>{new Date(item.dataHora).toLocaleDateString()}</span>
                        </div>
                      </div>
                      <div className="item-actions">
                        <button className="action-btn" onClick={() => handleEdit(item)}><Edit2 size={16} /></button>
                        <button className="action-btn delete" onClick={() => handleDelete(item.id)}><Trash2 size={16} /></button>
                      </div>
                    </div>
                  ))}
                </div>
              </section>

              {/* Form (Simplified for Dashboard) */}
              <section className="card form-section">
                <div className="card-header"><h2>{editingId ? 'Editar' : 'Novo Rápido'}</h2></div>
                <form onSubmit={handleSubmit} className="form-body">
                  <div className="input-group">
                    <label>Título</label>
                    <input type="text" className="input-field" name="titulo" value={formData.titulo} onChange={handleInputChange} required />
                  </div>
                  <div className="input-group">
                    <label>Data</label>
                    <input type="datetime-local" className="input-field" name="dataHora" value={formData.dataHora} onChange={handleInputChange} required />
                  </div>
                  <button type="submit" className="submit-btn"><Plus size={18} /> Salvar</button>
                </form>
              </section>
            </div>
          </>
        )}

        {activeTab === 'kanban' && (
          <div style={{ height: 'calc(100vh - 200px)' }}>
            <KanbanView compromissos={compromissos} onUpdate={handleUpdateItem} />
          </div>
        )}

        {activeTab === 'agenda' && (
          <AgendaView compromissos={compromissos} />
        )}

      </main>
    </div>
  );
}

export default App;
