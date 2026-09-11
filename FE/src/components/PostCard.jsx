import { useState } from 'react'
import { eliminaFoto, eliminaPost, urlImmagine } from '../services/api'
import Avatar from './Avatar'
import BottoneConferma from './BottoneConferma'
import Mappa from './Mappa'
import '../styles/post.css'

/**
 * Un post nel feed: autore, data, testo, luogo e foto con il testo OCR.
 * Le azioni (elimina post / elimina foto) chiamano il back-end e poi avvisano
 * il genitore con onCambiato() perche' ricarichi la lista dal server: cosi'
 * quello che si vede e' sempre lo stato vero, non una stima locale.
 */
function PostCard({ post, utenteCorrente, onCambiato }) {
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)
  const [mostraMappa, setMostraMappa] = useState(false)

  // Chi puo' cancellare: l'autore oppure un moderatore (stessa regola del back-end)
  const isAutore = post.autore.id === utenteCorrente.id
  const puoModificare = isAutore || utenteCorrente.ruolo === 'MODERATOR'

  const data = new Date(post.dataPubblicazione).toLocaleString('it-IT', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })

  async function esegui(azione) {
    setErrore('')
    setInCorso(true)
    try {
      await azione()
      onCambiato()
    } catch (e) {
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  return (
    <article className="post">
      <header className="post-testa">
        <Avatar nomeCompleto={post.autore.nomeCompleto} username={post.autore.username} />
        <div>
          <strong>{post.autore.nomeCompleto}</strong>
          <p className="nota">
            @{post.autore.username} · {data}
          </p>
        </div>
        {puoModificare && (
          <div className="post-azioni">
            <BottoneConferma etichetta="Elimina" onConferma={() => esegui(() => eliminaPost(post.id))} disabled={inCorso} />
          </div>
        )}
      </header>

      <p className="post-testo">{post.testo}</p>

      {post.posizione && (
        <Luogo posizione={post.posizione} mostraMappa={mostraMappa} onToggle={() => setMostraMappa(!mostraMappa)} />
      )}

      {post.foto.length > 0 && (
        <ul className="post-foto">
          {post.foto.map((foto) => (
            <FotoPost
              key={foto.id}
              foto={foto}
              puoEliminare={puoModificare}
              inCorso={inCorso}
              onElimina={() => esegui(() => eliminaFoto(foto.id))}
            />
          ))}
        </ul>
      )}

      {errore && <p className="errore">{errore}</p>}
    </article>
  )
}

// Nome del luogo (se c'e') e mini-mappa a richiesta
function Luogo({ posizione, mostraMappa, onToggle }) {
  let etichetta = posizione.nomeLuogo
  if (!etichetta) {
    // Punto senza indirizzo (mare, montagna): si mostrano le coordinate
    etichetta = `${posizione.latitudine.toFixed(4)}, ${posizione.longitudine.toFixed(4)}`
  }

  let etichettaToggle = 'Mostra mappa'
  if (mostraMappa) {
    etichettaToggle = 'Nascondi mappa'
  }

  return (
    <div className="post-luogo">
      <span>{etichetta}</span>
      <button type="button" className="mini" onClick={onToggle}>
        {etichettaToggle}
      </button>
      {mostraMappa && <Mappa punto={posizione} />}
    </div>
  )
}

// Una foto con il toggle del testo riconosciuto dall'OCR
function FotoPost({ foto, puoEliminare, inCorso, onElimina }) {
  const [mostraTesto, setMostraTesto] = useState(false)

  let etichettaToggle = 'Mostra testo riconosciuto'
  if (mostraTesto) {
    etichettaToggle = 'Nascondi testo'
  }

  // Se l'OCR non ha trovato nulla il bottone non ha senso
  const haTesto = Boolean(foto.testoOcr)

  return (
    <li className="foto">
      <img src={urlImmagine(foto.url)} alt="Foto del post" loading="lazy" />
      <div className="foto-azioni">
        {haTesto && (
          <button type="button" className="mini" onClick={() => setMostraTesto(!mostraTesto)}>
            {etichettaToggle}
          </button>
        )}
        {puoEliminare && (
          <button type="button" className="mini pericolo" onClick={onElimina} disabled={inCorso}>
            Rimuovi foto
          </button>
        )}
      </div>
      {/* pre-wrap: manda a capo le righe lunghe invece di far scorrere la pagina */}
      {mostraTesto && <pre className="foto-testo">{foto.testoOcr}</pre>}
    </li>
  )
}

export default PostCard
