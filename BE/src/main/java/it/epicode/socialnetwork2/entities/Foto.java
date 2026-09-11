package it.epicode.socialnetwork2.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "foto")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public class Foto {

	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE)
	private UUID id;

	// Percorso del file salvato su filesystem (cartella uploads/)
	@Column(nullable = false)
	private String url;

	// Testo estratto da Tesseract. TEXT e non @Lob: su Postgres @Lob creerebbe un oid
	@Column(name = "testo_ocr", columnDefinition = "TEXT")
	private String testoOcr;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	@ToString.Exclude
	private Post post;

	public Foto(String url, String testoOcr, Post post) {
		this.url = url;
		this.testoOcr = testoOcr;
		this.post = post;
	}
}
