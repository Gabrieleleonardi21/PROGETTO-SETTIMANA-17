package it.epicode.socialnetwork2.exceptions;

// Il servizio di geocoding (Google) non ha risposto o non e' configurato: 502
public class GeocodingException extends RuntimeException {

	public GeocodingException(String message) {
		super(message);
	}
}
