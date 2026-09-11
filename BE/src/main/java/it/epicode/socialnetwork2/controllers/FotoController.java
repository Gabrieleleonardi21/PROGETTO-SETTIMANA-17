package it.epicode.socialnetwork2.controllers;

import it.epicode.socialnetwork2.payloads.FotoResponse;
import it.epicode.socialnetwork2.services.FotoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FotoController {

	private final FotoService fotoService;

	public FotoController(FotoService fotoService) {
		this.fotoService = fotoService;
	}

	// Upload multipart (campo "file"): solo l'autore del post o un moderatore
	@PostMapping(path = "/posts/{postId}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('MODERATOR') or @postService.isAutore(#postId, authentication.principal)")
	@ResponseStatus(HttpStatus.CREATED)
	public FotoResponse aggiungi(@PathVariable UUID postId, @RequestParam("file") MultipartFile file) {
		return fotoService.aggiungi(postId, file);
	}

	@DeleteMapping("/foto/{id}")
	@PreAuthorize("hasRole('MODERATOR') or @fotoService.isAutore(#id, authentication.principal)")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void elimina(@PathVariable UUID id) {
		fotoService.elimina(id);
	}

	// Ricerca nel testo riconosciuto dall'OCR: GET /api/foto/cerca?testo=...
	@GetMapping("/foto/cerca")
	public List<FotoResponse> cerca(@RequestParam String testo) {
		return fotoService.cerca(testo);
	}
}
