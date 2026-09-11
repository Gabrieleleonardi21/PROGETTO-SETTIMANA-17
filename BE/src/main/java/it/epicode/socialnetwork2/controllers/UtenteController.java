package it.epicode.socialnetwork2.controllers;

import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.payloads.UtenteResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utenti")
public class UtenteController {

	// L'utente arriva gia' caricato dal JwtFilter: nessuna query aggiuntiva
	@GetMapping("/me")
	public UtenteResponse me(@AuthenticationPrincipal Utente utenteCorrente) {
		return UtenteResponse.from(utenteCorrente);
	}
}
