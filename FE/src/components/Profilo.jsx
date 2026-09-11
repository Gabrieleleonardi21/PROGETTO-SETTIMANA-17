import Avatar from './Avatar'
import '../styles/profilo.css'

// Scheda con i dati restituiti da GET /api/utenti/me
function Profilo({ utente }) {
  return (
    <section className="profilo">
      <Avatar nomeCompleto={utente.nomeCompleto} username={utente.username} grande />

      <div className="profilo-dati">
        <h2>{utente.nomeCompleto}</h2>
        <p className="nota">@{utente.username}</p>

        <dl>
          <dt>Email</dt>
          <dd>{utente.email}</dd>
          <dt>Ruolo</dt>
          <dd>{utente.ruolo}</dd>
        </dl>
      </div>
    </section>
  )
}

export default Profilo
