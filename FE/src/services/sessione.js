// Unico punto che sa DOVE e' salvato il token: se un giorno si passa a
// sessionStorage o a un cookie, si cambia solo qui.
const CHIAVE_TOKEN = 'accessToken'

// localStorage puo' non essere disponibile (navigazione privata con storage
// bloccato, iframe con permessi ridotti): ogni accesso sta in un try/catch
// cosi' l'app continua a funzionare, semplicemente senza ricordare il login.
export function leggiToken() {
  try {
    return localStorage.getItem(CHIAVE_TOKEN)
  } catch {
    return null
  }
}

export function salvaToken(token) {
  try {
    localStorage.setItem(CHIAVE_TOKEN, token)
  } catch {
    // niente storage: l'utente restera' loggato solo fino al reload
  }
}

export function cancellaToken() {
  try {
    localStorage.removeItem(CHIAVE_TOKEN)
  } catch {
    // vedi salvaToken
  }
}
