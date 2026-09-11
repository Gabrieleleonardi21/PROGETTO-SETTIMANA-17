import { useEffect, useRef, useState } from 'react'
import '../styles/form-post.css'

/**
 * Anteprima della webcam con pulsante di scatto.
 *
 * Restituisce lo scatto come File, cioe' nella stessa forma di un file scelto
 * dal disco: il resto dell'app non deve sapere da quale delle due sorgenti
 * arriva l'immagine.
 */
function Fotocamera({ onScatto, onChiudi }) {
  const videoRef = useRef(null)
  // Lo stream sta in un ref e non in uno stato: cambiarlo non deve ridisegnare
  // il componente, ma serve conservarlo per poterlo spegnere alla chiusura.
  const streamRef = useRef(null)
  const [errore, setErrore] = useState('')

  useEffect(() => {
    // Se l'utente chiude il riquadro mentre il browser sta ancora chiedendo il
    // permesso, la risposta arriva quando il componente non c'e' piu': questa
    // variabile evita di accendere una webcam che nessuno spegnera'.
    let annullato = false

    async function avvia() {
      // Fuori da un contesto sicuro mediaDevices non esiste (http://192.168.x.x)
      if (!navigator.mediaDevices) {
        setErrore('Fotocamera non disponibile: serve HTTPS oppure localhost')
        return
      }

      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          // environment = fotocamera posteriore sui telefoni; sui portatili
          // il browser ripiega su quella disponibile
          video: { facingMode: 'environment', width: { ideal: 1920 } },
        })

        if (annullato) {
          fermaStream(stream)
          return
        }

        streamRef.current = stream
        videoRef.current.srcObject = stream
      } catch (e) {
        setErrore(traduciErrore(e))
      }
    }

    avvia()

    // Pulizia: senza, la webcam resta accesa anche dopo aver chiuso il riquadro
    return () => {
      annullato = true
      fermaStream(streamRef.current)
    }
  }, [])

  function scatta() {
    const video = videoRef.current

    // Il fotogramma si copia su una canvas delle dimensioni del flusso video:
    // meno pixel significherebbe OCR peggiore
    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    canvas.getContext('2d').drawImage(video, 0, 0)

    canvas.toBlob(
      (blob) => {
        // Il terzo argomento e' il nome del file: un Blob non ne ha uno
        onScatto(new File([blob], `scatto-${Date.now()}.jpg`, { type: 'image/jpeg' }))
      },
      'image/jpeg',
      // 0.95: una compressione piu' forte spalma i contorni delle lettere
      0.95,
    )
  }

  return (
    <section className="fotocamera">
      {errore && <p className="errore">{errore}</p>}

      {/* playsInline: su iPhone, senza, il video parte a tutto schermo.
          muted evita che il browser blocchi l'avvio automatico. */}
      <video ref={videoRef} autoPlay playsInline muted />

      <div className="bottoni">
        <button type="button" className="primaria" onClick={scatta} disabled={Boolean(errore)}>
          Scatta
        </button>
        <button type="button" onClick={onChiudi}>
          Chiudi fotocamera
        </button>
      </div>
    </section>
  )
}

function fermaStream(stream) {
  if (!stream) {
    return
  }
  // Ogni traccia (video, eventuale audio) va fermata singolarmente
  stream.getTracks().forEach((traccia) => traccia.stop())
}

function traduciErrore(e) {
  if (e.name === 'NotAllowedError') {
    return 'Permesso negato: autorizza la fotocamera dalle impostazioni del browser'
  }
  if (e.name === 'NotFoundError') {
    return 'Nessuna fotocamera trovata su questo dispositivo'
  }
  if (e.name === 'NotReadableError') {
    return 'Fotocamera occupata da un altro programma'
  }
  return `Impossibile aprire la fotocamera: ${e.message}`
}

export default Fotocamera
