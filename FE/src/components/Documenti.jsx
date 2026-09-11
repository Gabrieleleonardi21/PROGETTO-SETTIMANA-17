import { useEffect, useState } from 'react'
import { caricaDocumento, controllaDimensione, eliminaDocumento, mieiDocumenti, urlImmagine } from '../services/api'
import BottoneConferma from './BottoneConferma'
import Fotocamera from './Fotocamera'
import '../styles/documenti.css'

/**
 * Sezione "Documenti" del profilo: carica un'immagine o un PDF (anche dalla
 * webcam), il back-end ne riconosce il testo con l'OCR e lo salva. Qui si
 * possono rileggere, cercare nel testo riconosciuto ed eliminare.
 */
function Documenti() {
  const [documenti, setDocumenti] = useState([])
  const [ricerca, setRicerca] = useState('')
  const [file, setFile] = useState(null)
  const [fotocameraAperta, setFotocameraAperta] = useState(false)
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)

  // Al montaggio: catena .then perche' la regola react-hooks/set-state-in-effect
  // vieta setState sincroni nell'effetto (i callback della promise girano dopo)
  useEffect(() => {
    let ignora = false
    mieiDocumenti()
      .then((lista) => {
        if (!ignora) {
          setDocumenti(lista)
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

  async function ricarica(testo) {
    try {
      setDocumenti(await mieiDocumenti(testo))
      setErrore('')
    } catch (e) {
      setErrore(e.message)
    }
  }

  function cerca(evento) {
    evento.preventDefault()
    ricarica(ricerca.trim())
  }

  // Vale sia per il file scelto dal disco sia per lo scatto della webcam
  function scegliFile(nuovo) {
    if (!nuovo) {
      return
    }
    const messaggio = controllaDimensione(nuovo)
    setErrore(messaggio)
    if (messaggio) {
      setFile(null)
      return
    }
    setFile(nuovo)
  }

  function usaScatto(scatto) {
    scegliFile(scatto)
    setFotocameraAperta(false)
  }

  async function carica(evento) {
    evento.preventDefault()
    if (!file) {
      setErrore('Scegli prima un file')
      return
    }
    setErrore('')
    setInCorso(true)
    try {
      await caricaDocumento(file)
      setFile(null)
      setRicerca('')
      await ricarica('')
    } catch (e) {
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  async function elimina(id) {
    setErrore('')
    try {
      await eliminaDocumento(id)
      await ricarica(ricerca.trim())
    } catch (e) {
      setErrore(e.message)
    }
  }

  let etichettaInvio = 'Carica e riconosci testo'
  if (inCorso) {
    etichettaInvio = 'Riconoscimento in corso...'
  }

  const vuoto = documenti.length === 0 && !errore

  return (
    <section className="documenti">
      <h2>Documenti</h2>
      <p className="nota">Carica un'immagine o un PDF: il testo viene riconosciuto automaticamente.</p>

      <form className="form-documento" onSubmit={carica}>
        <div className="bottoni">
          {/* label + input nascosto: l'input file nativo non si puo' stilizzare */}
          <label className="bottone-file">
            Scegli file
            <input
              type="file"
              accept="image/*,application/pdf"
              onChange={(e) => scegliFile(e.target.files[0])}
            />
          </label>
          <button type="button" onClick={() => setFotocameraAperta(!fotocameraAperta)}>
            Fotocamera
          </button>
        </div>

        {fotocameraAperta && <Fotocamera onScatto={usaScatto} onChiudi={() => setFotocameraAperta(false)} />}

        {file && (
          <p className="nota file-pronto">
            <span>File pronto: {file.name}</span>
            <button type="button" className="mini" onClick={() => setFile(null)}>
              Togli
            </button>
          </p>
        )}

        <button type="submit" className="primaria" disabled={inCorso || !file}>
          {etichettaInvio}
        </button>
      </form>

      <form className="form-ricerca" onSubmit={cerca}>
        <input
          value={ricerca}
          onChange={(e) => setRicerca(e.target.value)}
          placeholder="Cerca nel testo dei documenti..."
        />
        <button type="submit">Cerca</button>
      </form>

      {errore && <p className="errore">{errore}</p>}
      {vuoto && <p className="nota">Nessun documento.</p>}

      <ul className="lista-documenti">
        {documenti.map((d) => (
          <DocumentoCard key={d.id} documento={d} onElimina={() => elimina(d.id)} />
        ))}
      </ul>
    </section>
  )
}

// Un documento: titolo, peso, data, link al file, testo OCR a richiesta
function DocumentoCard({ documento, onElimina }) {
  const [mostraTesto, setMostraTesto] = useState(false)

  const data = new Date(documento.dataCaricamento).toLocaleString('it-IT', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })

  let etichettaToggle = 'Mostra testo riconosciuto'
  if (mostraTesto) {
    etichettaToggle = 'Nascondi testo'
  }

  const haTesto = Boolean(documento.testoOcr)

  return (
    <li className="documento">
      <div className="documento-testa">
        <div className="documento-dati">
          <strong>{documento.titolo}</strong>
          <p className="nota">
            {formattaPeso(documento.peso)} · {data}
          </p>
        </div>
        <BottoneConferma etichetta="Elimina" onConferma={onElimina} />
      </div>

      <div className="bottoni">
        {/* target _blank: il file (immagine o PDF) si apre in una nuova scheda */}
        <a className="link" href={urlImmagine(documento.url)} target="_blank" rel="noreferrer">
          Apri file
        </a>
        {haTesto && (
          <button type="button" className="mini" onClick={() => setMostraTesto(!mostraTesto)}>
            {etichettaToggle}
          </button>
        )}
      </div>

      {mostraTesto && <pre className="foto-testo">{documento.testoOcr}</pre>}
    </li>
  )
}

// 16159 -> "15,8 KB"
function formattaPeso(byte) {
  if (byte < 1024) {
    return `${byte} B`
  }
  if (byte < 1024 * 1024) {
    return `${(byte / 1024).toFixed(1)} KB`
  }
  return `${(byte / (1024 * 1024)).toFixed(1)} MB`
}

export default Documenti
