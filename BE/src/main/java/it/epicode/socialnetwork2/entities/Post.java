package it.epicode.socialnetwork2.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public class Post {

	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE)
	private UUID id;

	@Column(nullable = false, length = 200)
	private String testo;

	// Impostata alla creazione e mai piu' modificabile
	@Column(name = "data_pubblicazione", nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private Instant dataPubblicazione;

	// LAZY: l'autore viene caricato solo quando serve davvero
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "utente_id", nullable = false)
	@ToString.Exclude
	private Utente utente;

	// Facoltativa: un post puo' non avere un luogo (tutte le colonne restano null)
	@Embedded
	private Posizione posizione;

	public Post(String testo, Utente utente, Posizione posizione) {
		this.testo = testo;
		this.utente = utente;
		this.posizione = posizione;
		this.dataPubblicazione = Instant.now();
	}
}
