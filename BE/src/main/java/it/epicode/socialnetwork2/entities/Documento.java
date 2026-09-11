package it.epicode.socialnetwork2.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Documento personale caricato dal profilo (immagine o PDF) con il testo
 * riconosciuto dall'OCR. Privato: lo vede solo il proprietario.
 */
@Entity
@Table(name = "documenti")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public class Documento {

	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE)
	private UUID id;

	// Nome originale del file: e' quello che l'utente riconosce
	@Column(nullable = false)
	private String titolo;

	// Percorso del file salvato su filesystem (cartella uploads/)
	@Column(nullable = false)
	private String url;

	// Dimensione in byte
	@Column(nullable = false)
	private Long peso;

	// TEXT e non @Lob: su Postgres @Lob creerebbe un oid
	@Column(name = "testo_ocr", columnDefinition = "TEXT")
	private String testoOcr;

	@Column(name = "data_caricamento", nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private Instant dataCaricamento;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "utente_id", nullable = false)
	@ToString.Exclude
	private Utente utente;

	public Documento(String titolo, String url, Long peso, String testoOcr, Utente utente) {
		this.titolo = titolo;
		this.url = url;
		this.peso = peso;
		this.testoOcr = testoOcr;
		this.utente = utente;
		this.dataCaricamento = Instant.now();
	}
}
