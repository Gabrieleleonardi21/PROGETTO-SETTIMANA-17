package it.epicode.socialnetwork2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
// "user" e' una parola riservata in Postgres: la tabella si chiama "utenti"
@Table(name = "utenti")
// costruttore vuoto riservato a JPA: protected cosi' non puo' essere chiamato a mano
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public class Utente {

	@Id
	@GeneratedValue
	@Setter(AccessLevel.NONE) // l'id lo genera JPA, non deve essere riassegnabile
	private UUID id;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(name = "nome_completo", nullable = false, unique = true, length = 75)
	private String nomeCompleto;

	@Column(nullable = false, unique = true, length = 75)
	private String email;

	@Column(nullable = false)
	@JsonIgnore // la password non deve MAI finire in un JSON
	@ToString.Exclude // ...e nemmeno nei log tramite toString()
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Ruolo ruolo;

	// Il ruolo non e' nel costruttore: chi si registra e' sempre MEMBER
	public Utente(String username, String nomeCompleto, String email, String password) {
		this.username = username;
		this.nomeCompleto = nomeCompleto;
		this.email = email;
		this.password = password;
		this.ruolo = Ruolo.MEMBER;
	}
}
