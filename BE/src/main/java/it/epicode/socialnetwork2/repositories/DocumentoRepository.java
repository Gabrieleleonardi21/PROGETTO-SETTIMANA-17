package it.epicode.socialnetwork2.repositories;

import it.epicode.socialnetwork2.entities.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentoRepository extends JpaRepository<Documento, UUID> {

	// I documenti di un utente, dal piu' recente
	List<Documento> findByUtente_IdOrderByDataCaricamentoDesc(UUID utenteId);

	// Ricerca nel testo OCR, limitata ai documenti dell'utente
	List<Documento> findByUtente_IdAndTestoOcrContainingIgnoreCaseOrderByDataCaricamentoDesc(UUID utenteId, String testo);

	// Per cancellare tutto quando si elimina l'account
	List<Documento> findByUtente_Id(UUID utenteId);
}
