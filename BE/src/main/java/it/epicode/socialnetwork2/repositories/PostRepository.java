package it.epicode.socialnetwork2.repositories;

import it.epicode.socialnetwork2.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

	/**
	 * Il feed, dal piu' recente. Pageable aggiunge da solo LIMIT e OFFSET.
	 *
	 * JOIN FETCH carica l'autore nella stessa query: senza, avremmo il problema
	 * N+1 (una query per la pagina e poi una per l'autore di ogni post) e con
	 * open-in-view=false una LazyInitializationException nel costruire i DTO.
	 */
	@Query(value = """
			SELECT p FROM Post p
			JOIN FETCH p.utente
			ORDER BY p.dataPubblicazione DESC
			""",
			// il COUNT con JOIN FETCH non e' valido: la query per il totale va scritta a mano
			countQuery = "SELECT count(p) FROM Post p")
	Page<Post> feed(Pageable pageable);

	@Query(value = """
			SELECT p FROM Post p
			JOIN FETCH p.utente
			WHERE p.utente.id = :utenteId
			ORDER BY p.dataPubblicazione DESC
			""",
			countQuery = "SELECT count(p) FROM Post p WHERE p.utente.id = :utenteId")
	Page<Post> feedDiUtente(UUID utenteId, Pageable pageable);

	// Tutti i post di un utente, senza paginazione: serve a cancellarli quando si elimina l'account
	List<Post> findByUtente_Id(UUID utenteId);
}
