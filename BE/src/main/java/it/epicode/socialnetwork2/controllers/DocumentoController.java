package it.epicode.socialnetwork2.controllers;

import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.payloads.DocumentoResponse;
import it.epicode.socialnetwork2.services.DocumentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

// Documenti personali del profilo: ognuno vede e gestisce solo i propri
@RestController
@RequestMapping("/api/documenti")
public class DocumentoController {

	private final DocumentoService documentoService;

	public DocumentoController(DocumentoService documentoService) {
		this.documentoService = documentoService;
	}

	// Upload multipart (campo "file", immagine o PDF): OCR e salvataggio
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentoResponse carica(@RequestParam("file") MultipartFile file,
	                                @AuthenticationPrincipal Utente utenteCorrente) {
		return documentoService.carica(file, utenteCorrente);
	}

	// I miei documenti; ?testo= filtra sul testo riconosciuto
	@GetMapping
	public List<DocumentoResponse> miei(@RequestParam(required = false) String testo,
	                                    @AuthenticationPrincipal Utente utenteCorrente) {
		return documentoService.miei(utenteCorrente, testo);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('MODERATOR') or @documentoService.isProprietario(#id, authentication.principal)")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void elimina(@PathVariable UUID id) {
		documentoService.elimina(id);
	}
}
