import { useState } from 'react'
import { cercaFoto, urlImmagine } from '../services/api'
import '../styles/ricerca.css'

// Cerca una parola nel testo che l'OCR ha riconosciuto nelle foto
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
      setRisultati(await cercaFoto(testo.trim()))
    } catch (e) {
      setErrore(e.message)
    } finally {
      setInCorso(false)
    }
  }

  const nessunRisultato = risultati !== null && risultati.length === 0

  return (
    <main className="contenuto">
      <form className="form-ricerca" onSubmit={cerca}>
        <input
          value={testo}
          onChange={(e) => setTesto(e.target.value)}
          placeholder="Cerca nel testo delle foto..."
          required
        />
        <button type="submit" className="primaria" disabled={inCorso}>
          Cerca
        </button>
      </form>

      {errore && <p className="errore">{errore}</p>}
      {nessunRisultato && <p className="nota">Nessuna foto contiene "{testo}".</p>}

      {risultati && risultati.length > 0 && (
        <ul className="risultati">
          {risultati.map((foto) => (
            <li key={foto.id} className="risultato">
              <img src={urlImmagine(foto.url)} alt="Foto trovata" loading="lazy" />
              <pre className="foto-testo">{foto.testoOcr}</pre>
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}

export default Ricerca
