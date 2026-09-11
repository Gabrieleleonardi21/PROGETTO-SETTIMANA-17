import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login, profilo } from '../services/api'
import '../styles/auth.css'

/**
 * Pagina di accesso. Dopo il login il token e' gia' salvato da api.js:
 * qui basta chiedere il profilo e passarlo ad App, che tiene la sessione.
 */
function Login({ onEntrato }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)
  const navigate = useNavigate()

  // onSubmit e non onClick: cosi' funziona anche premendo Invio nei campi
  async function invia(evento) {
    evento.preventDefault()
    setErrore('')
    setInCorso(true)
    try {
      await login(email, password)
      const utente = await profilo()
      onEntrato(utente)
      navigate('/')
    } catch (e) {
      // Qui arriva il messaggio vero del back-end: "Email o password errati"
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  return (
    <main className="pagina-auth">
      <form className="riquadro" onSubmit={invia}>
        <p className="brand-auth">Social Network</p>
        <h1>Bentornato</h1>
        <p className="nota">Entra per vedere i post della community.</p>

        <label className="campo">
          <span>Email</span>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
            required
          />
        </label>

        <label className="campo">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>

        {errore && <p className="errore">{errore}</p>}

        {/* disabled durante la chiamata: evita il doppio invio */}
        <button type="submit" className="primaria" disabled={inCorso}>
          Entra
        </button>

        <p className="nota">
          Non hai un account? <Link className="link" to="/registrazione">Registrati</Link>
        </p>
      </form>
    </main>
  )
}

export default Login
