import '../styles/profilo.css'

// Quante tinte esistono in profilo.css (.avatar-tinta-0 ... .avatar-tinta-7)
const NUMERO_TINTE = 8

/**
 * Cerchio con le iniziali dell'utente. Non abbiamo immagini profilo nel
 * back-end, quindi le iniziali sono il modo piu' semplice per dare un volto
 * a ogni utente senza caricare nulla.
 * Il colore dipende dallo username: stesso utente = stesso colore ovunque.
 */
function Avatar({ nomeCompleto, username, grande }) {
  // Varianti calcolate prima del JSX: tinta e dimensione sono classi CSS,
  // niente style inline
  let classi = `avatar avatar-tinta-${tintaDi(username)}`
  if (grande) {
    classi += ' avatar-grande'
  }

  return (
    <span className={classi} aria-hidden="true">
      {iniziali(nomeCompleto)}
    </span>
  )
}

// Somma dei codici dei caratteri modulo 8: e' deterministica, quindi lo
// stesso username finisce sempre nella stessa tinta
function tintaDi(username) {
  if (!username) {
    return 0
  }
  let somma = 0
  for (const carattere of username) {
    somma += carattere.charCodeAt(0)
  }
  return somma % NUMERO_TINTE
}

// "Gabriele Leonardi" -> "GL"; "Moderatore" -> "M". Al massimo due lettere
// perche' oltre non stanno nel cerchio.
function iniziali(nome) {
  if (!nome) {
    return '?'
  }
  return nome
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((parola) => parola[0].toUpperCase())
    .join('')
}

export default Avatar
