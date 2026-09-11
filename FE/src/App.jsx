import { useEffect, useState } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import RottaProtetta from './components/RottaProtetta'
import Sidebar from './components/Sidebar'
import Home from './pages/Home'
import Login from './pages/Login'
import Moderazione from './pages/Moderazione'
import PaginaProfilo from './pages/PaginaProfilo'
import Registrazione from './pages/Registrazione'
import Ricerca from './pages/Ricerca'
import { EVENTO_SESSIONE_SCADUTA, profilo } from './services/api'
import { cancellaToken, leggiToken } from './services/sessione'
import { TEMA_CHIARO, TEMA_SCURO, applicaTema, leggiTema, salvaTema } from './services/tema'

/**
 * Tiene la sessione (l'utente loggato) e decide quale pagina mostrare.
 *
 * Il token vive in localStorage, ma non ci fidiamo di lui alla cieca: al
 * primo caricamento chiediamo GET /me. Se il back-end risponde 401 (token
 * scaduto o manomesso) lo buttiamo via e si torna al login.
 */
function App() {
  const [utente, setUtente] = useState(null)
  // true solo se c'e' un token da verificare: evita di mostrare il login per
  // un istante a chi in realta' e' gia' loggato
  const [inVerifica, setInVerifica] = useState(Boolean(leggiToken()))
  // Il tema e' gia' applicato in main.jsx: qui serve solo per il bottone
  const [tema, setTema] = useState(leggiTema)

  useEffect(() => {
    if (!leggiToken()) {
      return
    }

    async function verifica() {
      try {
        setUtente(await profilo())
      } catch {
        cancellaToken()
      } finally {
        setInVerifica(false)
      }
    }

    verifica()
  }, [])

  // api.js lancia questo evento su un 401 durante l'uso (token scaduto):
  // azzerando l'utente, RottaProtetta rimanda al login da sola
  useEffect(() => {
    function sessioneScaduta() {
      setUtente(null)
    }
    window.addEventListener(EVENTO_SESSIONE_SCADUTA, sessioneScaduta)
    return () => window.removeEventListener(EVENTO_SESSIONE_SCADUTA, sessioneScaduta)
  }, [])

  function esci() {
    cancellaToken()
    setUtente(null)
  }

  function cambiaTema() {
    let nuovo = TEMA_SCURO
    if (tema === TEMA_SCURO) {
      nuovo = TEMA_CHIARO
    }
    applicaTema(nuovo)
    salvaTema(nuovo)
    setTema(nuovo)
  }

  if (inVerifica) {
    return <p className="nota caricamento">Caricamento...</p>
  }

  // Chi e' gia' dentro non deve rivedere login/registrazione
  let paginaLogin = <Login onEntrato={setUtente} />
  let paginaRegistrazione = <Registrazione onEntrato={setUtente} />
  if (utente) {
    paginaLogin = <Navigate to="/" replace />
    paginaRegistrazione = <Navigate to="/" replace />
  }

  // La moderazione e' solo per i MODERATOR: gli altri vengono rimandati alla home
  let paginaModerazione = <Navigate to="/" replace />
  if (utente && utente.ruolo === 'MODERATOR') {
    paginaModerazione = <Moderazione utente={utente} />
  }

  return (
    <Routes>
      <Route path="/login" element={paginaLogin} />
      <Route path="/registrazione" element={paginaRegistrazione} />

      {/* Rotta "layout": Sidebar comune, dentro cambia solo la pagina */}
      <Route
        element={
          <RottaProtetta utente={utente}>
            <Sidebar utente={utente} onEsci={esci} tema={tema} onCambiaTema={cambiaTema} />
          </RottaProtetta>
        }
      >
        <Route path="/" element={<Home utente={utente} />} />
        <Route path="/profilo" element={<PaginaProfilo utente={utente} />} />
        <Route path="/ricerca" element={<Ricerca />} />
        <Route path="/moderazione" element={paginaModerazione} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
