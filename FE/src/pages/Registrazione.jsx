import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login, profilo, registra } from '../services/api'
import '../styles/auth.css'

// Stato iniziale del form in una costante: serve anche per resettarlo
const FORM_VUOTO = { username: '', nomeCompleto: '', email: '', password: '' }

/**
 * Pagina di registrazione. Il back-end su /register restituisce l'utente ma
 * non il token, quindi subito dopo facciamo il login con le stesse credenziali:
 * l'utente si ritrova dentro senza dover rifare l'accesso a mano.
 */
function Registrazione({ onEntrato }) {
  const [form, setForm] = useState(FORM_VUOTO)
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)
  const navigate = useNavigate()

  // Un solo handler per tutti i campi: il name dell'input e' la chiave del form
  function aggiorna(evento) {
    setForm({ ...form, [evento.target.name]: evento.target.value })
  }

  async function invia(evento) {
    evento.preventDefault()
    setErrore('')
    setInCorso(true)
    try {
      await registra(form)
      await login(form.email, form.password)
      const utente = await profilo()
      onEntrato(utente)
      navigate('/')
    } catch (e) {
      // Errori di validazione del back-end nel formato "campo: messaggio; ..."
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  return (
    <main className="pagina-auth">
      <form className="riquadro" onSubmit={invia}>
        <p className="brand-auth">Social Network</p>
        <h1>Crea un account</h1>
        <p className="nota">Bastano pochi dati per iniziare.</p>

        <label className="campo">
          <span>Username</span>
          <input name="username" value={form.username} onChange={aggiorna} autoComplete="username" required />
        </label>

        <label className="campo">
          <span>Nome completo</span>
          <input name="nomeCompleto" value={form.nomeCompleto} onChange={aggiorna} autoComplete="name" required />
        </label>

        <label className="campo">
          <span>Email</span>
          <input type="email" name="email" value={form.email} onChange={aggiorna} autoComplete="email" required />
        </label>

        <label className="campo">
          <span>Password</span>
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={aggiorna}
            autoComplete="new-password"
            required
          />
          {/* Stessa regola del @Pattern del back-end: meglio saperla prima del 400 */}
          <small className="nota">Almeno 8 caratteri, con una lettera e un numero.</small>
        </label>

        {errore && <p className="errore">{errore}</p>}

        <button type="submit" className="primaria" disabled={inCorso}>
          Registrati
        </button>

        <p className="nota">
          Hai già un account? <Link className="link" to="/login">Entra</Link>
        </p>
      </form>
    </main>
  )
}

export default Registrazione
