package it.epicode.socialnetwork2.controllers;

import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.payloads.UtenteResponse;
import it.epicode.socialnetwork2.services.UtenteService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/utenti")
public class UtenteController {

	private final UtenteService utenteService;

	public UtenteController(UtenteService utenteService) {
		this.utenteService = utenteService;
	}

	// L'utente arriva gia' caricato dal JwtFilter: nessuna query aggiuntiva
	@GetMapping("/me")
	public UtenteResponse me(@AuthenticationPrincipal Utente utenteCorrente) {
		return UtenteResponse.from(utenteCorrente);
	}

	// --- Solo MODERATOR ------------------------------------------------------

	@GetMapping
	@PreAuthorize("hasRole('MODERATOR')")
	public Page<UtenteResponse> findAll(@RequestParam(defaultValue = "0") int page,
	                                    @RequestParam(defaultValue = "20") int size) {
		return utenteService.findAll(page, size);
	}

	@PatchMapping("/{id}/promuovi")
	@PreAuthorize("hasRole('MODERATOR')")
	public UtenteResponse promuovi(@PathVariable UUID id) {
		return UtenteResponse.from(utenteService.promuoviAModeratore(id));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('MODERATOR')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void elimina(@PathVariable UUID id) {
		utenteService.elimina(id);
	}
}
