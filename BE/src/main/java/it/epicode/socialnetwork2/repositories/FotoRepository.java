package it.epicode.socialnetwork2.repositories;

import it.epicode.socialnetwork2.entities.Foto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FotoRepository extends JpaRepository<Foto, UUID> {

	List<Foto> findByPost_Id(UUID postId);

	// Una sola query per tutte le foto di una pagina di post (evita N+1)
	List<Foto> findByPost_IdIn(List<UUID> postIds);

	// LIKE %testo% case-insensitive sul testo OCR
	List<Foto> findByTestoOcrContainingIgnoreCase(String testo);
}
