package it.epicode.socialnetwork2.security;

import io.jsonwebtoken.Claims;
import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.exceptions.UnauthorizedException;
import it.epicode.socialnetwork2.repositories.UtenteRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Eseguito una volta per ogni richiesta protetta: legge il Bearer token,
 * lo verifica, carica l'utente e lo mette nel SecurityContext.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtTools jwtTools;
	private final UtenteRepository utenteRepository;
	// Serve per far passare le eccezioni dal filtro all'ExceptionsHandler (altrimenti uscirebbe un 500 vuoto)
	private final HandlerExceptionResolver exceptionResolver;

	public JwtFilter(JwtTools jwtTools, UtenteRepository utenteRepository,
	                 @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
		this.jwtTools = jwtTools;
		this.utenteRepository = utenteRepository;
		this.exceptionResolver = exceptionResolver;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		try {
			String authHeader = request.getHeader("Authorization");
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				throw new UnauthorizedException("Header Authorization mancante o non nel formato 'Bearer <token>'");
			}

			String accessToken = authHeader.substring(7);
			Claims claims = jwtTools.verifyToken(accessToken);
			UUID utenteId = UUID.fromString(claims.getSubject());

			Utente utente = utenteRepository.findById(utenteId)
					.orElseThrow(() -> new UnauthorizedException("L'utente associato al token non esiste"));

			// Il prefisso "ROLE_" e' obbligatorio: hasRole('MODERATOR') cerca l'authority "ROLE_MODERATOR"
			List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name()));

			// Il principal e' l'entita' Utente stessa: nei controller si recupera con @AuthenticationPrincipal
			SecurityContextHolder.getContext().setAuthentication(
					new UsernamePasswordAuthenticationToken(utente, null, authorities));

			filterChain.doFilter(request, response);
		} catch (Exception e) {
			exceptionResolver.resolveException(request, response, null, e);
		}
	}

	// Registrazione e login sono pubblici: il filtro non deve nemmeno cercare il token
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getServletPath().startsWith("/api/auth/");
	}
}
