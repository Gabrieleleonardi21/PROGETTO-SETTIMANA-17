package it.epicode.socialnetwork2.payloads;

import it.epicode.socialnetwork2.entities.Posizione;

public record PosizioneResponse(Double latitudine, Double longitudine, String nomeLuogo) {

	// Un post puo' non avere un luogo: in quel caso nel JSON esce null
	public static PosizioneResponse from(Posizione posizione) {
		if (posizione == null) {
			return null;
		}
		return new PosizioneResponse(posizione.getLatitudine(), posizione.getLongitudine(), posizione.getNomeLuogo());
	}
}
