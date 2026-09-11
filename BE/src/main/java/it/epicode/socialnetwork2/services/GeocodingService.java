package it.epicode.socialnetwork2.services;

import it.epicode.socialnetwork2.entities.Posizione;
import it.epicode.socialnetwork2.exceptions.GeocodingException;
import it.epicode.socialnetwork2.exceptions.NotFoundException;
import it.epicode.socialnetwork2.payloads.GoogleGeocodingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.regex.Pattern;

/**
 * Trasforma un nome di luogo ("Roma, Italia") in coordinate chiamando Google.
 * La chiave API sta solo qui, lato server: il client manda il nome del luogo.
 */
@Service
public class GeocodingService {

	private static final Pattern PLUS_CODE = Pattern.compile("^[23456789CFGHJMPQRVWX]{4,8}\\+[23456789CFGHJMPQRVWX]{2,}.*$");

	private final RestClient restClient;
	private final String apiKey;

	public GeocodingService(RestClient.Builder builder,
	                        @Value("${google.geocoding.api-key:}") String apiKey,
	                        @Value("${google.geocoding.base-url:https://geocode.googleapis.com}") String baseUrl) {
		this.restClient = builder.baseUrl(baseUrl).build();
		this.apiKey = apiKey;
	}

	public Posizione geocodifica(String nomeLuogo) {
		if (apiKey.isBlank()) {
			throw new GeocodingException("Chiave Google non configurata: manca GOOGLE_API_KEY in env.properties");
		}

		GoogleGeocodingResponse risposta = chiama(nomeLuogo);
		if (risposta == null || risposta.results() == null || risposta.results().isEmpty()) {
			throw new NotFoundException("Nessun risultato per il luogo: " + nomeLuogo);
		}

		// Si salva l'indirizzo formattato da Google, non quello scritto dall'utente:
		// "roma" diventa "Roma RM, Italia" e il feed resta uniforme
		GoogleGeocodingResponse.Result primo = risposta.results().get(0);
		return new Posizione(primo.location().latitude(), primo.location().longitude(), primo.formattedAddress());
	}

	/**
	 * Reverse geocoding: da un punto scelto sulla mappa al suo indirizzo.
	 * "Best effort": se Google non risponde o il punto non ha un indirizzo
	 * (mare, montagna) il post tiene comunque le coordinate, senza nome.
	 */
	public Posizione descriviPunto(Double latitudine, Double longitudine, String nomeDiRiserva) {
		String nomeLuogo = nomeDiRiserva;
		if (!apiKey.isBlank()) {
			try {
				GoogleGeocodingResponse risposta = restClient.get()
						.uri(uriBuilder -> uriBuilder
								.path("/v4/geocode/location/{lat},{lng}")
								.queryParam("languageCode", "it")
								.build(latitudine, longitudine))
						.header("X-Goog-Api-Key", apiKey)
						.retrieve()
						.body(GoogleGeocodingResponse.class);
				if (risposta != null && risposta.results() != null) {
					// I risultati vanno dal piu' preciso al piu' generico. Dove non c'e' un
					// indirizzo (mare, montagna) il primo e' un "plus code" tipo 8FGJ2222+22,
					// illeggibile: si prende il primo risultato con un nome vero
					// (es. "Subiaco RM, Italia"), altrimenti resta il nome di riserva
					for (GoogleGeocodingResponse.Result risultato : risposta.results()) {
						String indirizzo = risultato.formattedAddress();
						if (indirizzo != null && !PLUS_CODE.matcher(indirizzo).matches()) {
							nomeLuogo = indirizzo;
							break;
						}
					}
				}
			} catch (RestClientException e) {
				// il nome e' un di piu': le coordinate scelte dall'utente restano valide
			}
		}
		return new Posizione(latitudine, longitudine, nomeLuogo);
	}

	private GoogleGeocodingResponse chiama(String nomeLuogo) {
		try {
			return restClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/v4/geocode/address/{nomeLuogo}")
							.queryParam("regionCode", "IT")
							.queryParam("languageCode", "it")
							.build(nomeLuogo))
					.header("X-Goog-Api-Key", apiKey)
					.retrieve()
					.body(GoogleGeocodingResponse.class);
		} catch (RestClientException e) {
			throw new GeocodingException("Errore dal servizio di geocoding: " + e.getMessage());
		}
	}
}
