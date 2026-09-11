import { Navigate, Outlet } from 'react-router-dom'
import '../styles/layout.css'

/**
 * Avvolge le rotte che richiedono il login: se non c'e' un utente in sessione
 * rimanda a /login. replace: la pagina protetta non resta nella cronologia,
 * cosi' il tasto "indietro" dopo il login non riporta al redirect.
 *
 * Usata come rotta "layout": children e' la Sidebar comune, Outlet e' il
 * punto in cui il router inserisce la pagina figlia (Home, Profilo...).
 */
function RottaProtetta({ utente, children }) {
  if (!utente) {
    return <Navigate to="/login" replace />
  }
  return (
    <div className="app">
      {children}
      <div className="colonna">
        <Outlet />
      </div>
    </div>
  )
}

export default RottaProtetta
