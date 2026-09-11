import { useEffect, useState } from 'react'
import Avatar from '../components/Avatar'
import BottoneConferma from '../components/BottoneConferma'
import { eliminaUtente, listaUtenti, promuoviUtente } from '../services/api'
import '../styles/moderazione.css'

// Solo MODERATOR: elenco utenti con promozione ed eliminazione account
function Moderazione({ utente }) {
  const [utenti, setUtenti] = useState([])
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)

  async function ricarica() {
    try {
      const pagina = await listaUtenti()
      setUtenti(pagina.content)
      setErrore('')
    } catch (e) {
      setErrore(e.message)
    }
  }

  // Al montaggio: catena .then perche' la regola react-hooks/set-state-in-effect
  // vieta setState sincroni nell'effetto (i callback della promise girano dopo)
  useEffect(() => {
    let ignora = false
    listaUtenti()
      .then((pagina) => {
        if (!ignora) {
          setUtenti(pagina.content)
        }
      })
      .catch((e) => {
        if (!ignora) {
          setErrore(e.message)
        }
      })
    return () => {
      ignora = true
    }
  }, [])

  // Dopo ogni azione si ricarica dal server invece di aggiustare lo stato a mano
  async function esegui(azione) {
    setErrore('')
    setInCorso(true)
    try {
      await azione()
      await ricarica()
    } catch (e) {
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  return (
    <main className="contenuto">
      <h2>Utenti registrati</h2>
      {errore && <p className="errore">{errore}</p>}

      <ul className="utenti">
        {utenti.map((u) => (
          <RigaUtente
            key={u.id}
            u={u}
            isMe={u.id === utente.id}
            inCorso={inCorso}
            onPromuovi={() => esegui(() => promuoviUtente(u.id))}
            onElimina={() => esegui(() => eliminaUtente(u.id))}
          />
        ))}
      </ul>
    </main>
  )
}

function RigaUtente({ u, isMe, inCorso, onPromuovi, onElimina }) {
  const isMembro = u.ruolo === 'MEMBER'

  let classiBadge = 'badge'
  if (!isMembro) {
    classiBadge += ' moderatore'
  }

  return (
    <li className="utente">
      <Avatar nomeCompleto={u.nomeCompleto} username={u.username} />
      <div className="utente-dati">
        <strong>{u.nomeCompleto}</strong> <span className="nota">@{u.username}</span>
        <p className="nota">{u.email}</p>
      </div>
      <span className={classiBadge}>{u.ruolo}</span>
      {/* I moderatori non si promuovono ne' si eliminano (regola del back-end) */}
      {isMembro && !isMe && (
        <div className="bottoni">
          <button type="button" onClick={onPromuovi} disabled={inCorso}>
            Promuovi
          </button>
          <BottoneConferma etichetta="Elimina" onConferma={onElimina} disabled={inCorso} />
        </div>
      )}
    </li>
  )
}

export default Moderazione
