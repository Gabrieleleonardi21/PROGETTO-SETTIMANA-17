package it.epicode.socialnetwork2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import it.epicode.socialnetwork2.entities.Utente;
import it.epicode.socialnetwork2.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// Genera e verifica i token JWT firmati con la chiave segreta (spring.jwt.secret)
@Component
public class JwtTools {

	private static final long DURATA_TOKEN_MS = 1000L * 60 * 60 * 24 * 7; // 7 giorni

	private final SecretKey secretKey;

	public JwtTools(@Value("${spring.jwt.secret}") String secret) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
	}

	// Nel token mettiamo solo l'id: il resto lo rileggiamo dal DB ad ogni richiesta
	public String generateToken(Utente utente) {
		return Jwts.builder()
				.subject(utente.getId().toString())
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + DURATA_TOKEN_MS))
				.signWith(secretKey)
				.compact();
	}

	// Verifica firma e scadenza; se qualcosa non torna -> 401
	public Claims verifyToken(String accessToken) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(accessToken)
					.getPayload();
		} catch (JwtException e) {
			throw new UnauthorizedException("Il token fornito non è valido o è scaduto");
		}
	}
}
