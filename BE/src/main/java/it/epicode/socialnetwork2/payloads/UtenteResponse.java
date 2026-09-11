package it.epicode.socialnetwork2.payloads;

import it.epicode.socialnetwork2.entities.Ruolo;
import it.epicode.socialnetwork2.entities.Utente;

import java.util.UUID;

// Vista pubblica dell'utente: mai la password
public record UtenteResponse(UUID id, String username, String nomeCompleto, String email, Ruolo ruolo) {

	public static UtenteResponse from(Utente utente) {
		return new UtenteResponse(utente.getId(), utente.getUsername(), utente.getNomeCompleto(),
				utente.getEmail(), utente.getRuolo());
	}
}
