package it.epicode.socialnetwork2.services;

import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.exceptions.UnauthorizedException;
import it.epicode.socialnetwork2.payloads.LoginPayload;
import it.epicode.socialnetwork2.repositories.UtenteRepository;
import it.epicode.socialnetwork2.security.JwtTools;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UtenteRepository utenteRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTools jwtTools;

	public AuthService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder, JwtTools jwtTools) {
		this.utenteRepository = utenteRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTools = jwtTools;
	}

	// Stesso messaggio per email inesistente e password errata: non si rivela quale dei due e' sbagliato
	public String login(LoginPayload payload) {
		Utente utente = utenteRepository.findByEmail(payload.email())
				.orElseThrow(() -> new UnauthorizedException("Email o password errati"));

		if (!passwordEncoder.matches(payload.password(), utente.getPassword())) {
			throw new UnauthorizedException("Email o password errati");
		}

		return jwtTools.generateToken(utente);
	}
}
