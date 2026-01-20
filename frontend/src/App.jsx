import { useState, useEffect } from 'react'
import axios from 'axios'

function App() {
  const [mensagem, setMensagem] = useState('Carregando sistema...')

  useEffect(() => {
    // Tenta buscar dados do Backend
    axios.get('http://localhost:8080/api/status')
      .then(response => {
        setMensagem(response.data)
      })
      .catch(error => {
        console.error("Erro ao conectar:", error);
        setMensagem('Erro: O Backend não está respondendo.')
      })
  }, [])

  return (
    <div style={{ padding: '50px', fontFamily: 'Arial' }}>
      <h1>Painel de Controle - LifeOS</h1>
      <hr />
      <h3>Status do Servidor:</h3>
      <p style={{ color: 'blue', fontWeight: 'bold' }}>{mensagem}</p>
    </div>
  )
}

export default App
