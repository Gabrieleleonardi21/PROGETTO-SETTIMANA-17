// Tema chiaro/scuro. Il tema e' un attributo data-theme su <html>: i CSS
// ridefiniscono solo le variabili in [data-theme='dark'], i componenti non
// sanno nulla del tema.
const CHIAVE_TEMA = 'tema'

export const TEMA_CHIARO = 'light'
export const TEMA_SCURO = 'dark'

// Preferenza salvata, altrimenti quella del sistema operativo
export function leggiTema() {
  try {
    const salvato = localStorage.getItem(CHIAVE_TEMA)
    if (salvato === TEMA_CHIARO || salvato === TEMA_SCURO) {
      return salvato
    }
  } catch {
    // storage non disponibile: si usa la preferenza di sistema
  }
  if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return TEMA_SCURO
  }
  return TEMA_CHIARO
}

export function applicaTema(tema) {
  document.documentElement.dataset.theme = tema
}

export function salvaTema(tema) {
  try {
    localStorage.setItem(CHIAVE_TEMA, tema)
  } catch {
    // niente storage: il tema vale fino al reload
  }
}
