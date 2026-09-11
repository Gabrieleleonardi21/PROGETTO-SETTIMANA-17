import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { applicaTema, leggiTema } from './services/tema'
// Il CSS di Leaflet PRIMA del nostro: senza, le tessere della mappa si sparpagliano
import 'leaflet/dist/leaflet.css'
import './styles/index.css'

// Tema applicato PRIMA del primo render: altrimenti chi usa il tema scuro
// vedrebbe un lampo di pagina chiara ad ogni caricamento
applicaTema(leggiTema())

// StrictMode monta due volte i componenti in sviluppo: serve a scovare gli
// effetti senza pulizia. Non ha alcun effetto in produzione.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
