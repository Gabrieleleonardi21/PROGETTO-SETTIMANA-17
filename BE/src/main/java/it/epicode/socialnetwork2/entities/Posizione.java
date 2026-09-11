package it.epicode.socialnetwork2.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Posizione geografica di un Post. @Embeddable: non ha una tabella propria,
 * le sue colonne vengono "appiattite" dentro la tabella post.
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public class Posizione {

	private Double latitudine;

	private Double longitudine;

	// es. "Roma, Italia": e' cio' che l'utente scrive e che viene geocodificato
	@Column(name = "nome_luogo")
	private String nomeLuogo;

	public Posizione(Double latitudine, Double longitudine, String nomeLuogo) {
		this.latitudine = latitudine;
		this.longitudine = longitudine;
		this.nomeLuogo = nomeLuogo;
	}
}
