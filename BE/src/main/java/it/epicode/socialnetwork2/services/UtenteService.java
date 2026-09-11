package it.epicode.socialnetwork2.services;

import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.exceptions.BadRequestException;
import it.epicode.socialnetwork2.exceptions.NotFoundException;
import it.epicode.socialnetwork2.payloads.RegistrazionePayload;
import it.epicode.socialnetwork2.repositories.UtenteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UtenteService {

	private final UtenteRepository utenteRepository;
	private final PasswordEncoder passwordEncoder;

	public UtenteService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
		this.utenteRepository = utenteRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// Controlli sui campi unique fatti qui per dare un messaggio chiaro (400) invece del 409 del DB
	public Utente registra(RegistrazionePayload payload) {
		if (utenteRepository.existsByEmail(payload.email())) {
			throw new BadRequestException("L'email " + payload.email() + " è già in uso");
		}
		if (utenteRepository.existsByUsername(payload.username())) {
			throw new BadRequestException("Lo username " + payload.username() + " è già in uso");
		}
		if (utenteRepository.existsByNomeCompleto(payload.nomeCompleto())) {
			throw new BadRequestException("Il nome completo " + payload.nomeCompleto() + " è già in uso");
		}

		// La password viene salvata solo come hash BCrypt
		Utente utente = new Utente(payload.username(), payload.nomeCompleto(), payload.email(),
				passwordEncoder.encode(payload.password()));
		return utenteRepository.save(utente);
	}

	public Utente findById(UUID id) {
		return utenteRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Utente con id " + id + " non trovato"));
	}
}
