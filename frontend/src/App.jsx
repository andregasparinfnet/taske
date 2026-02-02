import { useState, useEffect } from 'react';
import * as api from './services/api';
import Login from './views/Login/Login';
import KanbanView from './views/Kanban/KanbanView';
import AgendaView from './views/Agenda/AgendaView';
import DashboardView from './views/Dashboard/DashboardView';
import Toast from './components/Toast/Toast';
import ConfirmModal from './components/ConfirmModal/ConfirmModal';
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
  List,
  BookOpen,
  MoreHorizontal,
  AlertTriangle
} from 'lucide-react';

function App() {
  const [user, setUser] = useState(null);
  const [compromissos, setCompromissos] = useState([]);
  const [activeTab, setActiveTab] = useState('agenda'); // agenda, dashboard, kanban
  const [formData, setFormData] = useState({
    titulo: '',
    dataHora: '',
    tipo: 'PERICIA',
    status: 'PENDENTE',
    valor: '',
    descricao: '',
    urgente: false
  });
  const [erro, setErro] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    const savedAuth = localStorage.getItem('auth');
    if (savedAuth) {
      const auth = JSON.parse(savedAuth);
      if (auth.token) {
        setUser(auth);
        api.setAccessToken(auth.token);
        carregarCompromissos();
      }
    }
  }, []);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
  };

  const carregarCompromissos = async () => {
    try {
      const data = await api.getCompromissos();
      setCompromissos(data);
    } catch (error) {
      if (error.response && error.response.status === 403) handleLogout();
    }
  };

  const handleLogin = (auth) => {
    setUser(auth);
    api.setAccessToken(auth.token);
    localStorage.setItem('auth', JSON.stringify(auth));
    carregarCompromissos();
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('auth');
    setCompromissos([]);
    api.clearAuth();
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErro('');
    try {
      if (editingId) {
        await api.updateCompromisso(editingId, formData);
        showToast('Compromisso atualizado com sucesso!');
        setEditingId(null);
      } else {
        await api.createCompromisso(formData);
        showToast('Compromisso criado com sucesso!');
      }
      setFormData({ titulo: '', dataHora: '', tipo: 'PERICIA', status: 'PENDENTE', valor: '', descricao: '', urgente: false });
      carregarCompromissos();
    } catch (error) {
      setErro("Erro ao salvar. Verifique os campos.");
      showToast('Erro ao salvar compromisso', 'error');
    }
  };

  const handleEdit = (item) => {
    setFormData({
      titulo: item.titulo,
      dataHora: item.dataHora,
      tipo: item.tipo,
      status: item.status || 'PENDENTE',
      valor: item.valor || '',
      descricao: item.descricao || '',
      urgente: item.urgente || false
    });
    setEditingId(item.id);
  };

  // Estado para o modal de exclusão
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);

  const handleDeleteClick = (id) => {
    setItemToDelete(id);
    setDeleteModalOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (!itemToDelete) return;

    try {
      await api.deleteCompromisso(itemToDelete);
      showToast('Compromisso excluído com sucesso!');
      carregarCompromissos();
    } catch (error) {
      console.error(error);
      showToast('Erro ao excluir compromisso', 'error');
    } finally {
      setDeleteModalOpen(false);
      setItemToDelete(null);
    }
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
      case 'ESTUDOS': return <BookOpen size={18} />;
      case 'OUTROS': return <MoreHorizontal size={18} />;
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
      {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

      <ConfirmModal
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        onConfirm={handleConfirmDelete}
        title="Excluir Compromisso"
        message="Tem certeza que deseja excluir este item? Esta ação não pode ser desfeita."
      />

      {/* Sidebar */}
      <nav className="sidebar">
        <div className="logo-container">
          <div className="logo-bg"><LayoutDashboard size={24} /></div>
          <span className="logo-text">LifeOS</span>
        </div>

        <div className="nav-menu">
          <button onClick={() => setActiveTab('agenda')} className={`nav-item ${activeTab === 'agenda' ? 'active' : ''}`}>
            <Calendar size={20} />
            <span className="sidebar-label">Agenda</span>
          </button>
          <button onClick={() => setActiveTab('dashboard')} className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}>
            <LayoutDashboard size={20} />
            <span className="sidebar-label">Dashboard</span>
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
            <h1>{activeTab === 'agenda' ? 'Minha Agenda' : activeTab === 'dashboard' ? 'Visão Geral' : 'Quadro de Atividades'}</h1>
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
                  {compromissos.length === 0 ? (
                    <div className="empty-state">
                      <p>Nenhum compromisso encontrado.</p>
                    </div>
                  ) : (
                    compromissos.map((item) => (
                      <div key={item.id} className="list-item">
                        <div className="item-icon-box">{getTagIcon(item.tipo)}</div>
                        <div className="item-content">
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                              <h3 className="item-title">{item.titulo}</h3>
                              {item.urgente && (
                                <span style={{
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: '4px',
                                  fontSize: '0.7rem',
                                  color: 'var(--danger)',
                                  background: 'rgba(239, 68, 68, 0.1)',
                                  padding: '2px 6px',
                                  borderRadius: '4px',
                                  fontWeight: '600'
                                }}>
                                  <AlertTriangle size={12} /> URGENTE
                                </span>
                              )}
                            </div>
                            {item.valor > 0 && <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--success)' }}>R$ {item.valor.toFixed(2)}</span>}
                          </div>
                          <div className="item-meta">
                            <span>{new Date(item.dataHora).toLocaleDateString()} {new Date(item.dataHora).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                            <span style={{ fontSize: '0.75rem', padding: '2px 6px', background: 'var(--bg-body)', borderRadius: '4px' }}>{item.status || 'PENDENTE'}</span>
                          </div>
                          {item.descricao && <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '4px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.descricao}</p>}
                        </div>
                        <div className="item-actions">
                          <button className="action-btn" onClick={() => handleEdit(item)}><Edit2 size={16} /></button>
                          <button className="action-btn delete" onClick={() => handleDeleteClick(item.id)}><Trash2 size={16} /></button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </section>

              {/* Form (Simplified for Dashboard) */}
              <section className="card form-section">
                <div className="card-header">
                  <h2>{editingId ? 'Editar Compromisso' : 'Novo Compromisso'}</h2>
                </div>
                <form onSubmit={handleSubmit} className="form-body">

                  <div className="input-group">
                    <label>Título</label>
                    <div className="input-wrapper">
                      <Edit2 size={18} />
                      <input
                        type="text"
                        className="input-field"
                        name="titulo"
                        placeholder="Ex: Reunião de Pauta"
                        value={formData.titulo}
                        onChange={handleInputChange}
                        required
                      />
                    </div>
                  </div>

                  <div className="input-group">
                    <label>Data e Hora</label>
                    <div className="input-wrapper">
                      <Calendar size={18} />
                      <input
                        type="datetime-local"
                        className="input-field"
                        name="dataHora"
                        value={formData.dataHora}
                        onChange={handleInputChange}
                        required
                      />
                    </div>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                    <div className="input-group">
                      <label>Tipo</label>
                      <div className="input-wrapper">
                        <Briefcase size={18} />
                        <select className="input-field" name="tipo" value={formData.tipo} onChange={handleInputChange}>
                          <option value="PERICIA">Perícia</option>
                          <option value="TRABALHO">Trabalho</option>
                          <option value="FAMILIA">Família</option>
                          <option value="FINANCEIRO">Financeiro</option>
                          <option value="ESTUDOS">Estudos</option>
                          <option value="OUTROS">Outros</option>
                        </select>
                      </div>
                    </div>

                    <div className="input-group">
                      <label>Valor (R$)</label>
                      <div className="input-wrapper">
                        <DollarSign size={18} />
                        <input
                          type="number"
                          step="0.01"
                          className="input-field"
                          name="valor"
                          placeholder="0.00"
                          value={formData.valor}
                          onChange={handleInputChange}
                        />
                      </div>
                    </div>
                  </div>

                  <div className="input-group" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <input
                      type="checkbox"
                      id="urgente"
                      name="urgente"
                      checked={formData.urgente}
                      onChange={handleInputChange}
                      style={{ width: '18px', height: '18px', cursor: 'pointer' }}
                    />
                    <label htmlFor="urgente" style={{ margin: 0, cursor: 'pointer', color: 'var(--danger)', fontWeight: 600 }}>Marcar como Urgente</label>
                  </div>

                  <div className="input-group">
                    <label>Descrição</label>
                    <textarea
                      className="input-field"
                      name="descricao"
                      rows="2"
                      placeholder="Detalhes adicionais..."
                      value={formData.descricao}
                      onChange={handleInputChange}
                      style={{ paddingLeft: '1rem' }} // Textarea doesn't need big left padding usually unless icon
                    ></textarea>
                  </div>

                  <button type="submit" className="submit-btn">
                    {editingId ? <CheckCircle size={18} /> : <Plus size={18} />}
                    {editingId ? 'Atualizar' : 'Salvar Compromisso'}
                  </button>

                  {editingId && (
                    <button type="button" onClick={() => { setEditingId(null); setFormData({ titulo: '', dataHora: '', tipo: 'PERICIA', status: 'PENDENTE', valor: '', descricao: '', urgente: false }); }} style={{ marginTop: '0.5rem', width: '100%', padding: '0.5rem', background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                      Cancelar Edição
                    </button>
                  )}

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
