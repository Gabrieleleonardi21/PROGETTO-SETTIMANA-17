package it.epicode.socialnetwork2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: il browser blocca una fetch verso un'origine diversa (5173 -> 3001)
 * se il server non dichiara esplicitamente di accettarla.
 * Spring Security la legge tramite .cors(Customizer.withDefaults()) in SecurityConfig.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	// Origine del front-end, da application.properties (app.cors.allowed-origin)
	private final String allowedOrigin;

	public CorsConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
		this.allowedOrigin = allowedOrigin;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins(allowedOrigin)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				// "*" serve per header non "semplici" come Authorization e Content-Type
				.allowedHeaders("*")
				// il browser ricorda la risposta alla preflight per un'ora
				.maxAge(3600);
	}
}
