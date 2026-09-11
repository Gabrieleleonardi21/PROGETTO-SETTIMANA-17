package it.epicode.socialnetwork2.runners;

import it.epicode.socialnetwork2.entities.Ruolo;
import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.repositories.UtenteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * All'avvio crea il moderatore con le credenziali di env.properties.
 * Idempotente: se esiste gia' (stessa email) non fa nulla.
 */
@Component
public class DataSeeder implements CommandLineRunner {

	private final UtenteRepository utenteRepository;
	private final PasswordEncoder passwordEncoder;
	private final String username;
	private final String email;
	private final String password;

	public DataSeeder(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder,
	                  @Value("${seed.moderator.username}") String username,
	                  @Value("${seed.moderator.email}") String email,
	                  @Value("${seed.moderator.password}") String password) {
		this.utenteRepository = utenteRepository;
		this.passwordEncoder = passwordEncoder;
		this.username = username;
		this.email = email;
		this.password = password;
	}

	@Override
	public void run(String... args) {
		if (utenteRepository.existsByEmail(email)) {
			System.out.println("[Seeder] Moderatore già presente: " + email);
			return;
		}
		Utente moderatore = new Utente(username, "Moderatore Sistema", email, passwordEncoder.encode(password));
		moderatore.setRuolo(Ruolo.MODERATOR);
		utenteRepository.save(moderatore);
		System.out.println("[Seeder] Moderatore creato: " + email);
	}
}
