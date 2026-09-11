package it.epicode.socialnetwork2.repositories;

import it.epicode.socialnetwork2.entities.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UtenteRepository extends JpaRepository<Utente, UUID> {

	Optional<Utente> findByEmail(String email);

	// Usati in registrazione per dare un 400 chiaro invece del 409 generico del DB
	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByNomeCompleto(String nomeCompleto);
}
