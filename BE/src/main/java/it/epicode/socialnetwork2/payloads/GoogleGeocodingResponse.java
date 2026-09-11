package it.epicode.socialnetwork2.payloads;

import java.util.List;

// Risposta della Google Geocoding API v4 (host geocode.googleapis.com):
// - campi camelCase (formattedAddress): niente @JsonProperty
// - non c'e' un campo "status": l'esito sta nello status HTTP
// - "luogo non trovato" e' un 200 con corpo {}, quindi results arriva null
// - dentro location i campi sono latitude/longitude
public record GoogleGeocodingResponse(List<Result> results) {

	public record Result(Location location, String formattedAddress) {
	}

	public record Location(Double latitude, Double longitude) {
	}
}
