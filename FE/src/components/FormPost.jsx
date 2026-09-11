import { useState } from 'react'
import { caricaFoto, controllaDimensione, creaPost } from '../services/api'
import Fotocamera from './Fotocamera'
import Mappa from './Mappa'
import '../styles/form-post.css'

const MAX_TESTO = 200

// Come si indica dove sono state scattate le foto
const MODO_INDIRIZZO = 'indirizzo'
const MODO_MAPPA = 'mappa'

/**
 * Nuovo post: testo, posizione facoltativa (indirizzo scritto oppure punto
 * scelto sulla mappa) e foto (da disco o dalla webcam).
 * Il back-end vuole due chiamate separate: prima il post (JSON), poi una
 * multipart per ogni foto. Qui l'utente le vede come un'azione sola.
 */
function FormPost({ onCreato }) {
  const [testo, setTesto] = useState('')
  const [modoPosizione, setModoPosizione] = useState(MODO_INDIRIZZO)
  const [nomeLuogo, setNomeLuogo] = useState('')
  const [punto, setPunto] = useState(null) // { latitudine, longitudine } scelto sulla mappa
  const [file, setFile] = useState([])
  const [fotocameraAperta, setFotocameraAperta] = useState(false)
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)

  // I file oltre il limite non entrano nemmeno nella lista: meglio saperlo
  // subito che dopo aver aspettato un upload destinato a fallire con 413
  function aggiungiFile(nuovi) {
    const errori = nuovi.map(controllaDimensione).filter(Boolean)
    const validi = nuovi.filter((f) => !controllaDimensione(f))
    setErrore(errori.join('; '))
    setFile((attuali) => [...attuali, ...validi])
  }

  function rimuoviFile(indice) {
    setFile((attuali) => attuali.filter((_, i) => i !== indice))
  }

  function usaScatto(scatto) {
    aggiungiFile([scatto])
    setFotocameraAperta(false)
  }

  // Le due modalita' sono alternative: si manda al server solo quella attiva
  function datiPosizione() {
    if (modoPosizione === MODO_MAPPA && punto) {
      return { latitudine: punto.latitudine, longitudine: punto.longitudine }
    }
    if (modoPosizione === MODO_INDIRIZZO && nomeLuogo.trim()) {
      return { nomeLuogo: nomeLuogo.trim() }
    }
    return {}
  }

  async function invia(evento) {
    evento.preventDefault()
    setErrore('')
    setInCorso(true)
    try {
      const post = await creaPost({ testo, ...datiPosizione() })

      // Le foto una alla volta: l'OCR e' lento e cosi' il server non le
      // elabora tutte insieme
      const fallite = []
      for (const f of file) {
        try {
          await caricaFoto(post.id, f)
        } catch (e) {
          fallite.push(`${f.name}: ${e.message}`)
        }
      }

      setTesto('')
      setNomeLuogo('')
      setPunto(null)
      setFile([])
      // Il post esiste anche se qualche foto e' fallita: si ricarica il feed comunque
      onCreato()

      if (fallite.length > 0) {
        setErrore('Post pubblicato, ma alcune foto non sono state caricate. ' + fallite.join('; '))
      }
    } catch (e) {
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  const caratteriRimasti = MAX_TESTO - testo.length
  let etichettaInvio = 'Pubblica'
  if (inCorso) {
    etichettaInvio = 'Pubblicazione...'
  }

  // Classi dei due bottoni di modalita', calcolate prima del JSX
  let classiIndirizzo = 'mini'
  let classiMappa = 'mini'
  if (modoPosizione === MODO_INDIRIZZO) {
    classiIndirizzo += ' attivo'
  } else {
    classiMappa += ' attivo'
  }

  return (
    <form className="form-post" onSubmit={invia}>
      <textarea
        value={testo}
        onChange={(e) => setTesto(e.target.value)}
        placeholder="A cosa stai pensando?"
        maxLength={MAX_TESTO}
        rows={3}
        required
      />
      <p className="nota contatore">{caratteriRimasti} caratteri rimasti</p>

      <div className="campo">
        <span>Dove sono state scattate le foto? (facoltativo)</span>
        <div className="scelta-posizione">
          <button type="button" className={classiIndirizzo} onClick={() => setModoPosizione(MODO_INDIRIZZO)}>
            Indirizzo
          </button>
          <button type="button" className={classiMappa} onClick={() => setModoPosizione(MODO_MAPPA)}>
            Punto sulla mappa
          </button>
        </div>
      </div>

      {modoPosizione === MODO_INDIRIZZO && (
        <input
          value={nomeLuogo}
          onChange={(e) => setNomeLuogo(e.target.value)}
          placeholder="es. Piazza del Colosseo, Roma"
          maxLength={100}
        />
      )}

      {modoPosizione === MODO_MAPPA && (
        <>
          <p className="nota">Clicca sulla mappa per scegliere il punto, poi trascinalo se serve.</p>
          <Mappa punto={punto} onScelta={setPunto} />
          {punto && (
            <p className="nota punto-scelto">
              <span>
                Punto scelto: {punto.latitudine.toFixed(5)}, {punto.longitudine.toFixed(5)}
              </span>
              <button type="button" className="mini" onClick={() => setPunto(null)}>
                Togli punto
              </button>
            </p>
          )}
        </>
      )}

      <div className="bottoni">
        {/* label + input nascosto: l'input file nativo non si puo' stilizzare */}
        <label className="bottone-file">
          Scegli foto
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={(e) => aggiungiFile(Array.from(e.target.files))}
          />
        </label>
        <button type="button" onClick={() => setFotocameraAperta(!fotocameraAperta)}>
          Fotocamera
        </button>
      </div>

      {fotocameraAperta && <Fotocamera onScatto={usaScatto} onChiudi={() => setFotocameraAperta(false)} />}

      {file.length > 0 && (
        <ul className="file-scelti">
          {file.map((f, indice) => (
            <li key={`${f.name}-${indice}`}>
              <span>{f.name}</span>
              <button type="button" className="mini" onClick={() => rimuoviFile(indice)}>
                Togli
              </button>
            </li>
          ))}
        </ul>
      )}

      {errore && <p className="errore">{errore}</p>}

      <button type="submit" className="primaria" disabled={inCorso}>
        {etichettaInvio}
      </button>
    </form>
  )
}

export default FormPost
