import { useEffect, useState } from 'react'
import { feed, feedDiUtente } from '../services/api'
import PostCard from './PostCard'
import '../styles/feed.css'

const DIMENSIONE_PAGINA = 10

/**
 * Lista paginata di post. Senza utenteId mostra il feed generale, con
 * utenteId solo i post di quell'utente (pagina profilo).
 *
 * Per ripartire da capo (es. dopo aver pubblicato) il genitore cambia la
 * "key" del componente: React lo rimonta e la prima pagina viene richiesta
 * di nuovo. Piu' semplice di un contatore da tenere sincronizzato.
 */
function Feed({ utenteCorrente, utenteId }) {
  const [post, setPost] = useState([])
  const [pagina, setPagina] = useState(0)
  const [ultima, setUltima] = useState(true)
  const [errore, setErrore] = useState('')
  // true dall'inizio: la prima pagina parte subito al montaggio
  const [inCorso, setInCorso] = useState(true)

  function chiediPagina(numeroPagina) {
    if (utenteId) {
      return feedDiUtente(utenteId, numeroPagina, DIMENSIONE_PAGINA)
    }
    return feed(numeroPagina, DIMENSIONE_PAGINA)
  }

  // Mette in stato una pagina ricevuta dal server.
  // Prima pagina: si rimpiazza tutto. Pagine successive: si accodano
  function applicaPagina(risposta, numeroPagina, sostituisci) {
    if (sostituisci) {
      setPost(risposta.content)
    } else {
      setPost((precedenti) => [...precedenti, ...risposta.content])
    }
    setPagina(numeroPagina)
    setUltima(risposta.last)
    setErrore('')
  }

  // Usata dagli eventi (carica altri, ricarica dopo un elimina)
  async function carica(numeroPagina, sostituisci) {
    setInCorso(true)
    try {
      applicaPagina(await chiediPagina(numeroPagina), numeroPagina, sostituisci)
    } catch (e) {
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  // Prima pagina al montaggio. Catena .then invece di await: la regola
  // react-hooks/set-state-in-effect vieta setState sincroni nell'effetto,
  // i callback della promise invece girano dopo.
  useEffect(() => {
    // StrictMode monta due volte: la risposta del primo montaggio va ignorata
    let ignora = false
    chiediPagina(0)
      .then((risposta) => {
        if (!ignora) {
          applicaPagina(risposta, 0, true)
        }
      })
      .catch((e) => {
        if (!ignora) {
          setErrore(e.message)
        }
      })
      .finally(() => {
        if (!ignora) {
          setInCorso(false)
        }
      })
    return () => {
      ignora = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Dopo un elimina si ricarica tutto da capo: la paginazione lato server
  // e' cambiata e accodare non avrebbe piu' senso
  function ricarica() {
    carica(0, true)
  }

  function caricaAltri() {
    carica(pagina + 1, false)
  }

  const vuoto = !inCorso && post.length === 0 && !errore

  return (
    <section className="feed">
      {errore && <p className="errore">{errore}</p>}
      {vuoto && <p className="nota feed-vuoto">Nessun post ancora. Scrivi il primo!</p>}

      {post.map((p) => (
        <PostCard key={p.id} post={p} utenteCorrente={utenteCorrente} onCambiato={ricarica} />
      ))}

      {inCorso && <p className="nota">Caricamento...</p>}

      {!ultima && (
        <button type="button" className="carica-altri" onClick={caricaAltri} disabled={inCorso}>
          Carica altri
        </button>
      )}
    </section>
  )
}

export default Feed
