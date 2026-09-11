package it.epicode.socialnetwork2.services;

import it.epicode.socialnetwork2.entities.Post;
import it.epicode.socialnetwork2.entities.Ruolo;
import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.exceptions.BadRequestException;
import it.epicode.socialnetwork2.exceptions.NotFoundException;
import it.epicode.socialnetwork2.payloads.RegistrazionePayload;
import it.epicode.socialnetwork2.payloads.UtenteResponse;
import it.epicode.socialnetwork2.repositories.PostRepository;
import it.epicode.socialnetwork2.repositories.UtenteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UtenteService {

	private final UtenteRepository utenteRepository;
	private final PostRepository postRepository;
	private final PasswordEncoder passwordEncoder;
	private final PostService postService;
	private final DocumentoService documentoService;

	public UtenteService(UtenteRepository utenteRepository, PostRepository postRepository,
	                     PasswordEncoder passwordEncoder, PostService postService,
	                     DocumentoService documentoService) {
		this.utenteRepository = utenteRepository;
		this.postRepository = postRepository;
		this.passwordEncoder = passwordEncoder;
		this.postService = postService;
		this.documentoService = documentoService;
	}

	// Controlli sui campi unique fatti qui per dare un messaggio chiaro (400) invece del 409 del DB
	public Utente registra(RegistrazionePayload payload) {
		if (utenteRepository.existsByEmail(payload.email())) {
			throw new BadRequestException("L'email " + payload.email() + " è già in uso");
		}
		if (utenteRepository.existsByUsername(payload.username())) {
			throw new BadRequestException("Lo username " + payload.username() + " è già in uso");
		}
		if (utenteRepository.existsByNomeCompleto(payload.nomeCompleto())) {
			throw new BadRequestException("Il nome completo " + payload.nomeCompleto() + " è già in uso");
		}

		// La password viene salvata solo come hash BCrypt
		Utente utente = new Utente(payload.username(), payload.nomeCompleto(), payload.email(),
				passwordEncoder.encode(payload.password()));
		return utenteRepository.save(utente);
	}

	// --- Operazioni del moderatore -------------------------------------------

	public Page<UtenteResponse> findAll(int page, int size) {
		return utenteRepository.findAll(PageRequest.of(page, size, Sort.by("username")))
				.map(UtenteResponse::from);
	}

	public Utente promuoviAModeratore(UUID id) {
		Utente utente = findById(id);
		if (utente.getRuolo() == Ruolo.MODERATOR) {
			throw new BadRequestException("L'utente " + utente.getUsername() + " è già moderatore");
		}
		utente.setRuolo(Ruolo.MODERATOR);
		return utenteRepository.save(utente);
	}

	// Cancella l'account con tutti i suoi post (e quindi le foto e i file su disco) e documenti.
	// I moderatori non si possono eliminare: semplificazione voluta, non esiste la retrocessione
	@Transactional
	public void elimina(UUID id) {
		Utente utente = findById(id);
		if (utente.getRuolo() == Ruolo.MODERATOR) {
			throw new BadRequestException("Non è possibile eliminare un moderatore");
		}
		for (Post post : postRepository.findByUtente_Id(id)) {
			postService.delete(post.getId());
		}
		documentoService.eliminaTuttiDi(id);
		utenteRepository.delete(utente);
	}

	public Utente findById(UUID id) {
		return utenteRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Utente con id " + id + " non trovato"));
	}
}
