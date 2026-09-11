import { useState } from 'react'
import { cercaFoto, mieiDocumenti, urlImmagine } from '../services/api'
import '../styles/ricerca.css'

/**
 * Cerca una parola nel testo riconosciuto dall'OCR. Due chiamate in parallelo
 * perche' le fonti sono diverse: le foto dei post sono pubbliche, i documenti
 * sono solo quelli dell'utente loggato (il back-end filtra per proprietario).
 */
function Ricerca() {
  const [testo, setTesto] = useState('')
  const [risultati, setRisultati] = useState(null) // null = nessuna ricerca fatta
  const [errore, setErrore] = useState('')
  const [inCorso, setInCorso] = useState(false)

  async function cerca(evento) {
    evento.preventDefault()
    setErrore('')
    setInCorso(true)
    try {
      const parola = testo.trim()
      const [foto, documenti] = await Promise.all([cercaFoto(parola), mieiDocumenti(parola)])
      setRisultati({ foto, documenti })
    } catch (e) {
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  const nessunRisultato =
    risultati !== null && risultati.foto.length === 0 && risultati.documenti.length === 0

  return (
    <main className="contenuto">
      <form className="form-ricerca" onSubmit={cerca}>
        <input
          value={testo}
          onChange={(e) => setTesto(e.target.value)}
          placeholder="Cerca nel testo di foto e documenti..."
          required
        />
        <button type="submit" className="primaria" disabled={inCorso}>
          Cerca
        </button>
      </form>

      {errore && <p className="errore">{errore}</p>}
      {nessunRisultato && <p className="nota">Nessuna foto o documento contiene "{testo}".</p>}

      {risultati && risultati.foto.length > 0 && (
        <>
          <h2>Foto dei post</h2>
          <ul className="risultati">
            {risultati.foto.map((foto) => (
              <li key={foto.id} className="risultato">
                <img src={urlImmagine(foto.url)} alt="Foto trovata" loading="lazy" />
                <pre className="foto-testo">{foto.testoOcr}</pre>
              </li>
            ))}
          </ul>
        </>
      )}

      {risultati && risultati.documenti.length > 0 && (
        <>
          <h2>I tuoi documenti</h2>
          <ul className="risultati">
            {risultati.documenti.map((documento) => (
              <li key={documento.id} className="risultato">
                <strong>{documento.titolo}</strong>
                {/* Il file puo' essere un PDF: si apre in una nuova scheda, non come <img> */}
                <a className="link" href={urlImmagine(documento.url)} target="_blank" rel="noreferrer">
                  Apri file
                </a>
                <pre className="foto-testo">{documento.testoOcr}</pre>
              </li>
            ))}
          </ul>
        </>
      )}
    </main>
  )
}

export default Ricerca
