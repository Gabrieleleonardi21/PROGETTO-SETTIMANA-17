package it.epicode.socialnetwork2.services;

import it.epicode.socialnetwork2.entities.Documento;
import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.exceptions.BadRequestException;
import it.epicode.socialnetwork2.exceptions.NotFoundException;
import it.epicode.socialnetwork2.payloads.DocumentoResponse;
import it.epicode.socialnetwork2.repositories.DocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentoService {

	private final DocumentoRepository documentoRepository;
	private final OcrService ocrService;
	private final FileStorageService fileStorageService;

	public DocumentoService(DocumentoRepository documentoRepository, OcrService ocrService,
	                        FileStorageService fileStorageService) {
		this.documentoRepository = documentoRepository;
		this.ocrService = ocrService;
		this.fileStorageService = fileStorageService;
	}

	@Transactional
	public DocumentoResponse carica(MultipartFile file, Utente proprietario) {
		controllaFile(file);
		byte[] contenuto = leggiContenuto(file);
		String estensione = estensioneDi(file.getOriginalFilename());

		// Prima l'OCR: se fallisce non resta su disco un file senza riga nel DB
		String testoOcr = ocrService.estraiTesto(contenuto, estensione);
		String url = fileStorageService.salva(contenuto, estensione);

		Documento documento = new Documento(file.getOriginalFilename(), url, file.getSize(), testoOcr, proprietario);
		return DocumentoResponse.from(documentoRepository.save(documento));
	}

	// I documenti del proprietario, con filtro facoltativo sul testo OCR
	@Transactional(readOnly = true)
	public List<DocumentoResponse> miei(Utente proprietario, String testo) {
		List<Documento> documenti;
		if (testo == null || testo.isBlank()) {
			documenti = documentoRepository.findByUtente_IdOrderByDataCaricamentoDesc(proprietario.getId());
		} else {
			documenti = documentoRepository
					.findByUtente_IdAndTestoOcrContainingIgnoreCaseOrderByDataCaricamentoDesc(proprietario.getId(), testo.trim());
		}
		return documenti.stream().map(DocumentoResponse::from).toList();
	}

	@Transactional
	public void elimina(UUID id) {
		Documento documento = findById(id);
		documentoRepository.delete(documento);
		fileStorageService.elimina(documento.getUrl());
	}

	// Usata da UtenteService quando si elimina l'account
	@Transactional
	public void eliminaTuttiDi(UUID utenteId) {
		for (Documento documento : documentoRepository.findByUtente_Id(utenteId)) {
			documentoRepository.delete(documento);
			fileStorageService.elimina(documento.getUrl());
		}
	}

	// Usato da @PreAuthorize: solo il proprietario (o un moderatore) puo' cancellare
	@Transactional(readOnly = true)
	public boolean isProprietario(UUID documentoId, Utente utente) {
		return findById(documentoId).getUtente().getId().equals(utente.getId());
	}

	public Documento findById(UUID id) {
		return documentoRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Documento con id " + id + " non trovato"));
	}

	private void controllaFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("Nessun file ricevuto: usa il campo 'file' del form");
		}
		String tipo = file.getContentType();
		boolean immagine = tipo != null && tipo.startsWith("image/");
		boolean pdf = "application/pdf".equals(tipo);
		if (!immagine && !pdf) {
			throw new BadRequestException("Sono accettati solo immagini (jpg, png, gif, bmp) e PDF");
		}
	}

	private byte[] leggiContenuto(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (IOException e) {
			throw new BadRequestException("File illeggibile");
		}
	}

	// Estensione con il punto (".pdf"), stringa vuota quando non c'e'
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
