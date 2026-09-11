import { cancellaToken, leggiToken, salvaToken } from './sessione'

// Punto unico da cui passano tutte le chiamate al back-end: l'header
// Authorization e la lettura degli errori si scrivono una volta sola qui.
export const API_URL = import.meta.env.VITE_API_URL

// Evento con cui api() avvisa App che il token non e' piu' valido (401 durante
// l'uso): App lo ascolta e riporta al login senza ricaricare la pagina
export const EVENTO_SESSIONE_SCADUTA = 'sessione-scaduta'

// Dimensione massima di un file caricabile. STESSO valore di
// spring.servlet.multipart.max-file-size in BE/application.properties (10MB):
// se cambi uno cambia anche l'altro. Il limite vero e' quello del back-end
// (risponde 413); questo serve solo ad avvisare subito, senza aspettare l'upload.
export const MAX_UPLOAD_BYTE = 10 * 1024 * 1024
export const MAX_UPLOAD_ETICHETTA = '10 MB'

// Messaggio d'errore se il file e' troppo grande, altrimenti stringa vuota
export function controllaDimensione(file) {
  if (file.size > MAX_UPLOAD_BYTE) {
    return `Il file ${file.name} supera i ${MAX_UPLOAD_ETICHETTA}`
  }
  return ''
}

export async function api(percorso, opzioni = {}) {
  const headers = { ...opzioni.headers }

  // Con FormData il Content-Type NON va impostato a mano: il browser deve
  // scriverlo lui perche' ci aggiunge il "boundary" che separa i campi.
  if (!(opzioni.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
  }

  // Login e registrazione non hanno token: il back-end li accetta senza header
  const token = leggiToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const risposta = await fetch(`${API_URL}${percorso}`, { ...opzioni, headers })

  // Token scaduto o manomesso mentre l'utente era dentro: si butta via e si
  // avvisa App. Il login (che risponde 401 per credenziali errate) non ha
  // token, quindi non entra qui.
  if (risposta.status === 401 && token) {
    cancellaToken()
    window.dispatchEvent(new Event(EVENTO_SESSIONE_SCADUTA))
  }

  // fetch NON lancia errore sugli status 4xx/5xx: senza questo controllo
  // un 401 arriverebbe al componente come se fosse una risposta valida.
  if (!risposta.ok) {
    throw new Error(await leggiMessaggioErrore(risposta))
  }

  // 204 (es. DELETE) non ha corpo: risposta.json() lancerebbe un SyntaxError
  if (risposta.status === 204) {
    return null
  }

  return risposta.json()
}

// Il back-end risponde sempre con { message, status, timestamp } grazie
// all'ExceptionsHandler. Se pero' l'errore arriva da altro (server spento,
// proxy), il corpo non e' JSON: in quel caso ripieghiamo sullo status.
async function leggiMessaggioErrore(risposta) {
  try {
    const corpo = await risposta.json()
    if (corpo.message) {
      return corpo.message
    }
  } catch {
    // corpo non leggibile come JSON: si usa il messaggio generico sotto
  }
  return `Errore ${risposta.status}`
}

// --- Autenticazione ---------------------------------------------------------

// Il token viene salvato subito: le chiamate successive lo troveranno da sole
export async function login(email, password) {
  const risposta = await api('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
  salvaToken(risposta.accessToken)
  return risposta.accessToken
}

// dati = { username, nomeCompleto, email, password }
export function registra(dati) {
  return api('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(dati),
  })
}

// --- Utenti -----------------------------------------------------------------

export function profilo() {
  return api('/api/utenti/me')
}

// Solo MODERATOR
export function listaUtenti(page = 0, size = 50) {
  return api(`/api/utenti?page=${page}&size=${size}`)
}

export function promuoviUtente(id) {
  return api(`/api/utenti/${id}/promuovi`, { method: 'PATCH' })
}

export function eliminaUtente(id) {
  return api(`/api/utenti/${id}`, { method: 'DELETE' })
}

// --- Post -------------------------------------------------------------------

// Ritorna la Page di Spring: { content, totalElements, last, number, ... }
export function feed(page = 0, size = 10) {
  return api(`/api/posts?page=${page}&size=${size}`)
}

export function feedDiUtente(utenteId, page = 0, size = 10) {
  return api(`/api/posts/utente/${utenteId}?page=${page}&size=${size}`)
}

// dati = { testo, nomeLuogo } (nomeLuogo facoltativo: il back-end lo geocodifica)
export function creaPost(dati) {
  return api('/api/posts', {
    method: 'POST',
    body: JSON.stringify(dati),
  })
}

export function eliminaPost(id) {
  return api(`/api/posts/${id}`, { method: 'DELETE' })
}

// --- Foto -------------------------------------------------------------------

// Un file per chiamata, nel campo "file" del form: il back-end fa l'OCR e salva
export function caricaFoto(postId, file) {
  const form = new FormData()
  form.append('file', file)
  return api(`/api/posts/${postId}/foto`, { method: 'POST', body: form })
}

export function eliminaFoto(id) {
  return api(`/api/foto/${id}`, { method: 'DELETE' })
}

// Ricerca nel testo riconosciuto dall'OCR
export function cercaFoto(testo) {
  return api(`/api/foto/cerca?testo=${encodeURIComponent(testo)}`)
}

// --- Documenti (profilo) ----------------------------------------------------

// Immagine o PDF nel campo "file": il back-end fa l'OCR e salva
export function caricaDocumento(file) {
  const form = new FormData()
  form.append('file', file)
  return api('/api/documenti', { method: 'POST', body: form })
}

// I miei documenti, con filtro facoltativo sul testo riconosciuto
export function mieiDocumenti(testo = '') {
  let percorso = '/api/documenti'
  if (testo) {
    percorso += `?testo=${encodeURIComponent(testo)}`
  }
  return api(percorso)
}

export function eliminaDocumento(id) {
  return api(`/api/documenti/${id}`, { method: 'DELETE' })
}

// Le immagini sono servite dal back-end su /uploads/...: serve l'url assoluto per <img src>
export function urlImmagine(url) {
  return `${API_URL}${url}`
}
