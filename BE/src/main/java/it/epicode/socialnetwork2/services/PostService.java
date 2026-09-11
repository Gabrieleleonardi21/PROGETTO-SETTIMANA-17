package it.epicode.socialnetwork2.services;

import it.epicode.socialnetwork2.entities.Foto;
import it.epicode.socialnetwork2.entities.Posizione;
import it.epicode.socialnetwork2.entities.Post;
import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.exceptions.BadRequestException;
import it.epicode.socialnetwork2.exceptions.NotFoundException;
import it.epicode.socialnetwork2.payloads.PostPayload;
import it.epicode.socialnetwork2.payloads.PostResponse;
import it.epicode.socialnetwork2.repositories.FotoRepository;
import it.epicode.socialnetwork2.repositories.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

	private final PostRepository postRepository;
	private final FotoRepository fotoRepository;
	private final GeocodingService geocodingService;
	private final FileStorageService fileStorageService;

	public PostService(PostRepository postRepository, FotoRepository fotoRepository,
	                   GeocodingService geocodingService, FileStorageService fileStorageService) {
		this.postRepository = postRepository;
		this.fotoRepository = fotoRepository;
		this.geocodingService = geocodingService;
		this.fileStorageService = fileStorageService;
	}

	@Transactional
	public PostResponse create(PostPayload payload, Utente autore) {
		Post post = postRepository.save(new Post(payload.testo(), autore, posizioneDa(payload)));
		return PostResponse.from(post, List.of());
	}

	@Transactional(readOnly = true)
	public Page<PostResponse> feed(int page, int size) {
		return conFoto(postRepository.feed(PageRequest.of(page, size)));
	}

	@Transactional(readOnly = true)
	public Page<PostResponse> feedDiUtente(UUID utenteId, int page, int size) {
		return conFoto(postRepository.feedDiUtente(utenteId, PageRequest.of(page, size)));
	}

	@Transactional(readOnly = true)
	public PostResponse findResponseById(UUID id) {
		Post post = findById(id);
		return PostResponse.from(post, fotoRepository.findByPost_Id(id));
	}

	@Transactional
	public PostResponse update(UUID id, PostPayload payload) {
		// entita' gia' gestita da JPA: basta il setter, il save rende esplicita la modifica
		Post post = findById(id);
		post.setTesto(payload.testo());
		post.setPosizione(posizioneDa(payload));
		postRepository.save(post);
		return PostResponse.from(post, fotoRepository.findByPost_Id(id));
	}

	// Niente cascade JPA: le foto si cancellano a mano perche' vanno rimossi
	// anche i file su disco, cosa che il cascade non farebbe
	@Transactional
	public void delete(UUID id) {
		Post post = findById(id);
		List<Foto> foto = fotoRepository.findByPost_Id(id);
		fotoRepository.deleteAll(foto);
		foto.forEach(f -> fileStorageService.elimina(f.getUrl()));
		postRepository.delete(post);
	}

	// Usato da @PreAuthorize nei controller: "hasRole('MODERATOR') or @postService.isAutore(#id, authentication.principal)"
	@Transactional(readOnly = true)
	public boolean isAutore(UUID postId, Utente utente) {
		return findById(postId).getUtente().getId().equals(utente.getId());
	}

	public Post findById(UUID id) {
		return postRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Post con id " + id + " non trovato"));
	}

	/**
	 * Un punto scelto sulla mappa (lat + long) ha la precedenza sull'indirizzo
	 * scritto; se c'e' solo l'indirizzo lo si geocodifica; senza nulla il post
	 * non ha posizione e Google non viene nemmeno chiamato.
	 */
	private Posizione posizioneDa(PostPayload payload) {
		boolean haLatitudine = payload.latitudine() != null;
		boolean haLongitudine = payload.longitudine() != null;
		if (haLatitudine != haLongitudine) {
			throw new BadRequestException("Latitudine e longitudine vanno indicate entrambe");
		}

		String nomeLuogo = null;
		if (payload.nomeLuogo() != null && !payload.nomeLuogo().isBlank()) {
			nomeLuogo = payload.nomeLuogo().trim();
		}

		if (haLatitudine) {
			return geocodingService.descriviPunto(payload.latitudine(), payload.longitudine(), nomeLuogo);
		}
		if (nomeLuogo != null) {
			return geocodingService.geocodifica(nomeLuogo);
		}
		return null;
	}

	// Carica le foto di tutta la pagina con una sola query e le distribuisce ai post
	private Page<PostResponse> conFoto(Page<Post> pagina) {
		List<UUID> ids = pagina.getContent().stream().map(Post::getId).toList();
		Map<UUID, List<Foto>> fotoPerPost = fotoRepository.findByPost_IdIn(ids).stream()
				.collect(Collectors.groupingBy(foto -> foto.getPost().getId()));
		return pagina.map(post -> PostResponse.from(post, fotoPerPost.getOrDefault(post.getId(), List.of())));
	}
}
