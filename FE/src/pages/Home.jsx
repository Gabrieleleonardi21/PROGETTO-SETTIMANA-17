import { useState } from 'react'
import Feed from '../components/Feed'
import FormPost from '../components/FormPost'

// Pagina principale: form per un nuovo post e feed di tutti
function Home({ utente }) {
  // Cambiare la key rimonta il Feed, che cosi' riparte dalla prima pagina
  // e mostra subito il post appena pubblicato
  const [versione, setVersione] = useState(0)

  return (
    <main className="contenuto">
      <FormPost onCreato={() => setVersione(versione + 1)} />
      <Feed key={versione} utenteCorrente={utente} />
    </main>
  )
}

export default Home
