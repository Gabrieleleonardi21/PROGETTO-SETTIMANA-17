import L from 'leaflet'
import { MapContainer, Marker, TileLayer, useMapEvents } from 'react-leaflet'
import '../styles/mappa.css'

// Tessere standard di OpenStreetMap: gratuite e senza chiave API (le CARTO
// usate in U5D1 ora mostrano il watermark "API KEY REQUIRED")
const TILE_URL = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
const TILE_ATTRIBUZIONE = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'

// Centro di partenza quando non c'e' ancora un punto: Italia intera
const CENTRO_ITALIA = [42.5, 12.5]
const ZOOM_ITALIA = 5
const ZOOM_PUNTO = 14

// Icona del marker fatta in CSS (classe .pin-mappa): le immagini di default
// di Leaflet non vengono trovate dal bundler e il marker sparirebbe
const ICONA_PIN = L.divIcon({ className: 'pin-mappa', iconSize: [18, 18], iconAnchor: [9, 9] })

/**
 * Mappa con un solo punto.
 * - Con onScelta: e' un selettore. Click sulla mappa o trascinamento del
 *   marker chiamano onScelta({ latitudine, longitudine }).
 * - Senza onScelta: e' una mappa di sola visualizzazione (dentro un post).
 */
function Mappa({ punto, onScelta }) {
  const interattiva = Boolean(onScelta)

  let centro = CENTRO_ITALIA
  let zoom = ZOOM_ITALIA
  if (punto) {
    centro = [punto.latitudine, punto.longitudine]
    zoom = ZOOM_PUNTO
  }

  let classi = 'mappa'
  if (interattiva) {
    classi += ' mappa-selettore'
  }

  // Il marker si puo' trascinare solo nel selettore
  let eventiMarker = {}
  if (interattiva) {
    eventiMarker = {
      dragend: (e) => {
        const posizione = e.target.getLatLng()
        onScelta({ latitudine: posizione.lat, longitudine: posizione.lng })
      },
    }
  }

  return (
    <div className={classi}>
      <MapContainer
        center={centro}
        zoom={zoom}
        scrollWheelZoom={interattiva}
        dragging={interattiva}
        zoomControl={interattiva}
        doubleClickZoom={interattiva}
      >
        <TileLayer url={TILE_URL} attribution={TILE_ATTRIBUZIONE} />
        {interattiva && <ClickSullaMappa onScelta={onScelta} />}
        {punto && (
          <Marker
            position={[punto.latitudine, punto.longitudine]}
            icon={ICONA_PIN}
            draggable={interattiva}
            eventHandlers={eventiMarker}
          />
        )}
      </MapContainer>
    </div>
  )
}

// Componente "invisibile": serve solo ad agganciare gli eventi della mappa,
// che react-leaflet espone tramite hook e non come prop di MapContainer
function ClickSullaMappa({ onScelta }) {
  useMapEvents({
    click: (e) => onScelta({ latitudine: e.latlng.lat, longitudine: e.latlng.lng }),
  })
  return null
}

export default Mappa
