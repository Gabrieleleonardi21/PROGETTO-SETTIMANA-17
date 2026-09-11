import { useState } from 'react'

/**
 * Bottone per le azioni distruttive in due passi: al primo click chiede
 * conferma inline, al secondo esegue. Niente window.confirm(): blocca la
 * pagina e non si puo' stilizzare.
 */
function BottoneConferma({ etichetta, onConferma, disabled }) {
  const [inAttesa, setInAttesa] = useState(false)

  if (!inAttesa) {
    return (
      <button type="button" className="pericolo" onClick={() => setInAttesa(true)} disabled={disabled}>
        {etichetta}
      </button>
    )
  }

  return (
    <span className="bottoni">
      <button type="button" className="pericolo" onClick={onConferma} disabled={disabled}>
        Confermi?
      </button>
      <button type="button" onClick={() => setInAttesa(false)} disabled={disabled}>
        Annulla
      </button>
    </span>
  )
}

export default BottoneConferma
