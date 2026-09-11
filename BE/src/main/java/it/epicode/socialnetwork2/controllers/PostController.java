package it.epicode.socialnetwork2.controllers;

import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.payloads.PostPayload;
import it.epicode.socialnetwork2.payloads.PostResponse;
import it.epicode.socialnetwork2.services.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	// CREAZIONE: basta essere autenticati. L'autore e' l'utente del token, non e' nel payload
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PostResponse create(@RequestBody @Validated PostPayload payload,
	                           @AuthenticationPrincipal Utente utenteCorrente) {
		return postService.create(payload, utenteCorrente);
	}

	// FEED paginato, dal piu' recente
	@GetMapping
	public Page<PostResponse> feed(@RequestParam(defaultValue = "0") int page,
	                               @RequestParam(defaultValue = "10") int size) {
		return postService.feed(page, size);
	}

	@GetMapping("/{id}")
	public PostResponse findById(@PathVariable UUID id) {
		return postService.findResponseById(id);
	}

	@GetMapping("/utente/{utenteId}")
	public Page<PostResponse> feedDiUtente(@PathVariable UUID utenteId,
	                                       @RequestParam(defaultValue = "0") int page,
	                                       @RequestParam(defaultValue = "10") int size) {
		return postService.feedDiUtente(utenteId, page, size);
	}

	// MODIFICA e CANCELLAZIONE: autorizzazione sulla PROPRIETA' del post, oppure ruolo moderatore
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('MODERATOR') or @postService.isAutore(#id, authentication.principal)")
	public PostResponse update(@PathVariable UUID id, @RequestBody @Validated PostPayload payload) {
		return postService.update(id, payload);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('MODERATOR') or @postService.isAutore(#id, authentication.principal)")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		postService.delete(id);
	}
}
