import Documenti from '../components/Documenti'
import Feed from '../components/Feed'
import Profilo from '../components/Profilo'

// Scheda dell'utente loggato, i suoi documenti (con OCR) e i suoi post
function PaginaProfilo({ utente }) {
  return (
    <main className="contenuto">
      <Profilo utente={utente} />
      <Documenti />
      <h2>I tuoi post</h2>
      <Feed utenteCorrente={utente} utenteId={utente.id} />
    </main>
  )
}

export default PaginaProfilo
