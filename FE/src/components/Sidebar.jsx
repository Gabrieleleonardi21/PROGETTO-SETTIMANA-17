import { Link, NavLink } from 'react-router-dom'
import { TEMA_SCURO } from '../services/tema'
import Avatar from './Avatar'
import '../styles/sidebar.css'

/**
 * Colonna di navigazione delle pagine protette: brand, link, scheda
 * dell'utente loggato, cambio tema ed uscita. Su mobile il CSS la
 * trasforma in una barra in basso (vedi sidebar.css).
 */
function Sidebar({ utente, onEsci, tema, onCambiaTema }) {
  const isModeratore = utente.ruolo === 'MODERATOR'

  // Classe e testi calcolati prima del JSX, senza ternari
  let classiBadge = 'badge'
  if (isModeratore) {
    classiBadge += ' moderatore'
  }

  let etichettaTema = 'Tema scuro'
  if (tema === TEMA_SCURO) {
    etichettaTema = 'Tema chiaro'
  }

  return (
    <aside className="sidebar">
      <Link className="brand" to="/">
        Social Network
      </Link>

      {/* NavLink aggiunge da solo la classe "active" alla rotta corrente */}
      <nav className="menu">
        <NavLink to="/" end>
          Home
        </NavLink>
        <NavLink to="/profilo">Profilo</NavLink>
        <NavLink to="/ricerca">Ricerca</NavLink>
        {isModeratore && <NavLink to="/moderazione">Moderazione</NavLink>}
      </nav>

      <div className="utente-corrente">
        <Avatar nomeCompleto={utente.nomeCompleto} username={utente.username} />
        <div className="dati">
          <span className="nome">{utente.nomeCompleto}</span>
          <p className="nota">@{utente.username}</p>
        </div>
        <span className={classiBadge}>{utente.ruolo}</span>
      </div>

      <div className="azioni-sidebar">
        <button type="button" onClick={onCambiaTema}>
          {etichettaTema}
        </button>
        <button type="button" onClick={onEsci}>
          Esci
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
