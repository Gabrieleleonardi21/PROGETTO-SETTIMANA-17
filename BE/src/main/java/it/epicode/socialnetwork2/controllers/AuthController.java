package it.epicode.socialnetwork2.controllers;

import it.epicode.socialnetwork2.payloads.LoginPayload;
import it.epicode.socialnetwork2.payloads.LoginResponse;
import it.epicode.socialnetwork2.payloads.RegistrazionePayload;
import it.epicode.socialnetwork2.payloads.UtenteResponse;
import it.epicode.socialnetwork2.services.AuthService;
import it.epicode.socialnetwork2.services.UtenteService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// Endpoint pubblici: sono gli unici esclusi dal JwtFilter e in permitAll() nella SecurityConfig
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final UtenteService utenteService;

	public AuthController(AuthService authService, UtenteService utenteService) {
		this.authService = authService;
		this.utenteService = utenteService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UtenteResponse register(@RequestBody @Validated RegistrazionePayload payload) {
		return UtenteResponse.from(utenteService.registra(payload));
	}

	@PostMapping("/login")
	public LoginResponse login(@RequestBody @Validated LoginPayload payload) {
		return new LoginResponse(authService.login(payload));
	}
}
