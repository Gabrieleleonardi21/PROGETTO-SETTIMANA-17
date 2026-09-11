package it.epicode.socialnetwork2.services;

import it.epicode.socialnetwork2.entities.Foto;
import it.epicode.socialnetwork2.entities.Post;
import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.exceptions.BadRequestException;
import it.epicode.socialnetwork2.exceptions.NotFoundException;
import it.epicode.socialnetwork2.payloads.FotoResponse;
import it.epicode.socialnetwork2.repositories.FotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FotoService {

	private final FotoRepository fotoRepository;
	private final PostService postService;
	private final OcrService ocrService;
	private final FileStorageService fileStorageService;

	public FotoService(FotoRepository fotoRepository, PostService postService,
	                   OcrService ocrService, FileStorageService fileStorageService) {
		this.fotoRepository = fotoRepository;
		this.postService = postService;
		this.ocrService = ocrService;
		this.fileStorageService = fileStorageService;
	}

	// Il controllo "autore o moderatore" e' nel @PreAuthorize del controller
	@Transactional
	public FotoResponse aggiungi(UUID postId, MultipartFile file) {
		Post post = postService.findById(postId);
		controllaFile(file);
		byte[] contenuto = leggiContenuto(file);

		// Prima l'OCR: se fallisce non resta su disco un file senza riga nel DB
		String testoOcr = ocrService.estraiTesto(contenuto);
		String url = fileStorageService.salva(contenuto, estensioneDi(file.getOriginalFilename()));

		return FotoResponse.from(fotoRepository.save(new Foto(url, testoOcr, post)));
	}

	@Transactional
	public void elimina(UUID id) {
		Foto foto = findById(id);
		fotoRepository.delete(foto);
		fileStorageService.elimina(foto.getUrl());
	}

	// Ricerca full-text nel testo riconosciuto
	@Transactional(readOnly = true)
	public List<FotoResponse> cerca(String testo) {
		return fotoRepository.findByTestoOcrContainingIgnoreCase(testo).stream()
				.map(FotoResponse::from)
				.toList();
	}

	// Usato da @PreAuthorize: la foto appartiene a chi ha scritto il post
	@Transactional(readOnly = true)
	public boolean isAutore(UUID fotoId, Utente utente) {
		return findById(fotoId).getPost().getUtente().getId().equals(utente.getId());
	}

	public Foto findById(UUID id) {
		return fotoRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Foto con id " + id + " non trovata"));
	}

	private void controllaFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("Nessun file ricevuto: usa il campo 'file' del form");
		}
		String tipo = file.getContentType();
		if (tipo == null || !tipo.startsWith("image/")) {
			throw new BadRequestException("Sono accettate solo immagini (jpg, png, gif, bmp)");
		}
	}

	private byte[] leggiContenuto(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (IOException e) {
			throw new BadRequestException("File illeggibile");
		}
	}

	// Estensione con il punto (".png"), stringa vuota quando non c'e'
	private String estensioneDi(String nomeFile) {
		if (nomeFile == null) {
			return "";
		}
		int punto = nomeFile.lastIndexOf('.');
		if (punto < 0) {
			return "";
		}
		return nomeFile.substring(punto).toLowerCase();
	}
}
